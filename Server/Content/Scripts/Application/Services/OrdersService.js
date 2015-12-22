'use strict';
app.factory('OrdersService', ['$http', function ($http) {

    var serviceBase = 'http://localhost:56184/';
    var ordersServiceFactory = {};

    var _getOrders = function () {

        return $http.get(serviceBase + 'api/orders').then(function (results) {
            return results;
        });
    };

    ordersServiceFactory.getOrders = _getOrders;

    return ordersServiceFactory;

}]);