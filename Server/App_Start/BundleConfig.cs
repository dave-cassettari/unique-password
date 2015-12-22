using System.Web.Optimization;

namespace UniquePassword.Server
{
    public class BundleConfig
    {
        // For more information on bundling, visit http://go.microsoft.com/fwlink/?LinkId=301862
        public static void RegisterBundles(BundleCollection bundles)
        {
            bundles.Add(new ScriptBundle("~/bundles/jquery").Include(
                        "~/Content/Scripts/Vendor/jquery-{version}.js"));

            bundles.Add(new ScriptBundle("~/bundles/jqueryval").Include(
                        "~/Content/Scripts/Vendor/jquery.validate*"));

            // Use the development version of Modernizr to develop with and learn from. Then, when you're
            // ready for production, use the build tool at http://modernizr.com to pick only the tests you need.
            bundles.Add(new ScriptBundle("~/bundles/modernizr").Include(
                        "~/Content/Scripts/Vendor/modernizr-*"));

            bundles.Add(new ScriptBundle("~/bundles/bootstrap").Include(
                      "~/Content/Scripts/Vendor/bootstrap.js"));

            bundles.Add(new ScriptBundle("~/bundles/application")
                .Include("~/Content/Scripts/Vendor/angular.min.js")
                .Include("~/Content/Scripts/Vendor/angular-route.min.js")
                .Include("~/Content/Scripts/Vendor/angular-local-storage.min.js")
                .Include("~/Content/Scripts/Vendor/loading-bar.js")
                .Include("~/Content/Scripts/Application/app.js")
                .Include("~/Content/Scripts/Application/Services/AuthService.js")
                .Include("~/Content/Scripts/Application/Services/OrdersService.js")
                .Include("~/Content/Scripts/Application/Services/AuthInterceptorService.js")
                .Include("~/Content/Scripts/Application/Controllers/HomeController.js")
                .Include("~/Content/Scripts/Application/Controllers/IndexController.js")
                .Include("~/Content/Scripts/Application/Controllers/LoginController.js")
                .Include("~/Content/Scripts/Application/Controllers/SignupController.js")
                .Include("~/Content/Scripts/Application/Controllers/OrdersController.js"));

            bundles.Add(new StyleBundle("~/bundles/css").Include(
                      "~/Content/Styles/Vendor/bootstrap.css",
                      "~/Content/Styles/Vendor/loading-bar.css",
                      "~/Content/Styles/site.css"));
        }
    }
}
