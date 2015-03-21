var app = angular.module('app', []),
    tab = null;

app.controller('PasswordController', ['$scope', function($scope) {

  $scope.safeApply = function(fn) {
    var phase = this.$root.$$phase;

    if(phase == '$apply' || phase == '$digest') {
      if(fn && (typeof(fn) === 'function')) {
        fn();
      }
    } else {
      this.$apply(fn);
    }
  };

  angular.extend($scope, {
    master: '',
    domain: '',
    hashed: '',
    version: '1',
    revealed: false,
    haveInput: true,
    generate: function() {
      var plain = $scope.master + $scope.domain + $scope.version;

      $scope.hashed = plain;

      return $scope.hashed;
    },
    populate: function() {
      var password = $scope.generate();
      
      chrome.tabs.sendMessage(tab.id, { id: 'set-password', password: password }, function(response) {
        if (response.success) {
          window.close();
        } else {
          $scope.safeApply(function() {
            $scope.haveInput = false;
          });
        }
      });
    },
    toggle: function() {
      $scope.revealed = !$scope.revealed;
    }
  });

  addEventListener('load', function (event) {
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
      tab = tabs[0];

      chrome.tabs.sendMessage(tab.id, { id: 'is-opening' });

      $scope.safeApply(function() {
        var url = new URL("http://www.google.com");
        
        $scope.domain = url.hostname;
      });
    });
  }, true);

}]);

app.directive('selected', function() {
  return {
    link: function($scope, $element, attrs) {
      $scope.$watch('revealed', function(newValue) {
        if (newValue) {
          $element.focus();
          $element.select();
        }
      });
    }
  };
})