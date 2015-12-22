using System.Threading.Tasks;
using System.Web.Http;
using UniquePassword.Server.Models.Repositories;

namespace UniquePassword.Server.Controllers
{
    [RoutePrefix("api/tokens")]
    public class RefreshTokensController : ApiController
    {
        private readonly AuthRepository _repository = null;

        public RefreshTokensController()
        {
            _repository = new AuthRepository();
        }

        [Route]
        [Authorize(Users = "Admin")]
        public IHttpActionResult Get()
        {
            return Ok(_repository.GetAllRefreshTokens());
        }

        [Route]
        [Authorize(Users = "Admin")]
        public async Task<IHttpActionResult> Delete(string tokenId)
        {
            var result = await _repository.RemoveRefreshToken(tokenId);

            if (result)
            {
                return Ok();
            }

            return BadRequest("Token Id does not exist");
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