using Microsoft.AspNet.Identity;
using System.Threading.Tasks;
using System.Web.Http;
using UniquePassword.Server.Models.Entities;
using UniquePassword.Server.Models.Repositories;

namespace UniquePassword.Server.Controllers
{
    [RoutePrefix("api/account")]
    public class AccountController : ApiController
    {
        private AuthRepository _repository = null;

        public AccountController()
        {
            _repository = new AuthRepository();
        }

        [AllowAnonymous]
        [Route("register")]
        public async Task<IHttpActionResult> Register(UserModel userModel)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var result = await _repository.RegisterUser(userModel);
            var error = GetErrorResult(result);

            if (error != null)
            {
                return error;
            }

            return Ok();
        }

        private IHttpActionResult GetErrorResult(IdentityResult result)
        {
            if (result == null)
            {
                return InternalServerError();
            }

            if (!result.Succeeded)
            {
                if (result.Errors != null)
                {
                    foreach (var error in result.Errors)
                    {
                        ModelState.AddModelError("", error);
                    }
                }

                if (ModelState.IsValid)
                {
                    return BadRequest();
                }

                return BadRequest(ModelState);
            }

            return null;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                _repository.Dispose();
            }

            base.Dispose(disposing);
        }
    }
}