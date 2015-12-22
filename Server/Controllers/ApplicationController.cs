using System.Web.Mvc;

namespace UniquePassword.Server.Controllers
{
    public class ApplicationController : Controller
    {
        [AllowAnonymous]
        [Route]
        public ActionResult Index()
        {
            return View();
        }
    }
}