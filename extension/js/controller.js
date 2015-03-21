var app = angular.module('app', []),
    tab = null;

app.service('password', function() {
  return {
    master: '',
    domain: '',
    hashed: '',
    version: '1',
    generate: function() {
      var plain = this.master + this.domain + this.version;

      this.hashed = Sha256.hash(plain);

      return this.hashed;
    },
  };
});

app.controller('ExtensionController', ['$scope', 'password', function($scope, password) {
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
    password: password,
    revealed: false,
    haveInput: true,
    populate: function() {
      var hashed = password.generate();
      
      chrome.tabs.sendMessage(tab.id, { id: 'set-password', password: hashed }, function(response) {
        if (response && response.success) {
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
        
        $scope.password.domain = url.hostname;
      });
    });
  }, true);
}]);

app.controller('ApplicationController', ['$scope', 'password', function($scope, password) {
  angular.extend($scope, {
    password: password,
    revealed: false,
    populate: function() {
      password.generate();
    },
    toggle: function() {
      $scope.revealed = !$scope.revealed;
    }
  });
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