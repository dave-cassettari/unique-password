using Microsoft.AspNet.Identity;
using Server.Models.Contexts;
using Server.Models.Entities;
using System;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web.Http;

namespace Server.Controllers
{
    [RoutePrefix("api/domains")]
    public class DomainsController : ApiController
    {
        public const string AnonymousUserID = "anonymous";

        private readonly DatabaseContext _context;

        public DomainsController()
        {
            _context = new DatabaseContext();
        }

        [HttpGet]
        [Route("")]
        public IHttpActionResult Get()
        {
            var userID = GetUserID();
            var domains = _context.Domains
                .Where(x => x.UserID == userID)
                .OrderBy(x => x.LastModifiedOn);

            return Ok(domains);
        }

        [HttpGet]
        [Route("")]
        public IHttpActionResult Get(string website)
        {
            var domain = GetDomain(website);

            if (domain == null)
            {
                return NotFound();
            }

            return Ok(domain);
        }

        [HttpPost]
        [Route("")]
        public IHttpActionResult Create([FromBody]CreateViewModel viewModel)
        {
            var userID = GetUserID();
            var domain = new Domain()
            {
                UserID = userID,
                Website = viewModel.Website,
                MaximumLength = viewModel.MaximumLength,
                LastModifiedOn = DateTime.UtcNow,
                SpecialCharacters = viewModel.SpecialCharacters,
            };

            _context.Domains.Add(domain);

            if (_context.SaveChanges() != 1)
            {
                return BadRequest();
            }

            return Ok(domain);
        }

        [HttpPut]
        [Route("")]
        public IHttpActionResult Update(string website, [FromBody]UpdateViewModel viewModel)
        {
            var domain = GetDomain(website);

            if (domain == null)
            {
                var createViewModel = new CreateViewModel()
                {
                    Website = website,
                    MaximumLength = viewModel.MaximumLength,
                    SpecialCharacters = viewModel.SpecialCharacters,
                };

                return Create(createViewModel);
            }

            domain.MaximumLength = viewModel.MaximumLength;
            domain.LastModifiedOn = DateTime.UtcNow;
            domain.SpecialCharacters = viewModel.SpecialCharacters;

            if (_context.SaveChanges() != 1)
            {
                return BadRequest();
            }

            return Ok(domain);
        }

        [HttpDelete]
        [Route("")]
        public IHttpActionResult Delete(string website)
        {
            var domain = GetDomain(website);

            if (domain == null)
            {
                return NotFound();
            }

            _context.Domains.Remove(domain);

            if (_context.SaveChanges() != 1)
            {
                return BadRequest();
            }

            return Ok(true);
        }

        private Domain GetDomain(string website)
        {
            var userID = GetUserID();

            if (userID == null || website == null)
            {
                return null;
            }

            return _context.Domains
                .Where(x => x.UserID.ToUpper() == userID.ToUpper())
                .Where(x => x.Website.ToUpper() == website.ToUpper())
                .FirstOrDefault();
        }

        private string GetUserID()
        {
            var identity = User.Identity;

            if (identity.IsAuthenticated)
            {
                return User.Identity.GetUserId();
            }

            return AnonymousUserID;
        }

        protected override void Dispose(bool disposing)
        {
            base.Dispose(disposing);

            if (disposing)
            {
                if (_context != null)
                {
                    _context.Dispose();
                }
            }
        }

        public class CreateViewModel : UpdateViewModel
        {
            [Required]
            public string Website { get; set; }
        }

        public class UpdateViewModel
        {
            public int? MaximumLength { get; set; }

            public string SpecialCharacters { get; set; }
        }
    }
}
