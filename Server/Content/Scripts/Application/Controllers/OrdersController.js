'use strict';
app.controller('OrdersController', ['$scope', 'OrdersService', function ($scope, OrdersService) {

    $scope.orders = [];

    OrdersService.getOrders().then(function (results) {

        $scope.orders = results.data;

    }, function (error) {
        //alert(error.data.message);
    });

}]);