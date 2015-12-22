var app = angular.module('AuthenticationApplication', ['ngRoute', 'LocalStorageModule', 'angular-loading-bar']);

app.config(function ($routeProvider) {

    $routeProvider.when("/home", {
        controller: "HomeController",
        templateUrl: "/Content/Scripts/Application/Views/home.html"
    });

    $routeProvider.when("/login", {
        controller: "LoginController",
        templateUrl: "/Content/Scripts/Application/Views/login.html"
    });

    $routeProvider.when("/signup", {
        controller: "SignupController",
        templateUrl: "/Content/Scripts/Application/Views/signup.html"
    });

    $routeProvider.when("/orders", {
        controller: "OrdersController",
        templateUrl: "/Content/Scripts/Application/Views/orders.html"
    });

    $routeProvider.otherwise({ redirectTo: "/home" });
});

app.config(function ($httpProvider) {
    $httpProvider.interceptors.push('AuthInterceptorService');
});

app.run(['AuthService', function (authService) {
    authService.fillAuthData();
}]);