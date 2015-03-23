var app = angular.module('app', []),
    tab = null;

function safeApply($scope, fn) {
  var phase;

  if (this.$root) {
    phase = this.$root.$$phase;
  } else {
    phase = this.$$phase;
  }

  if(phase == '$apply' || phase == '$digest') {
    if(fn && (typeof(fn) === 'function')) {
      fn();
    }
  } else {
    this.$apply(fn);
  }
};

app.service('password', ['$rootScope', function($rootScope) {
  var service = {
    length: null,
    master: '',
    domain: '',
    hashed: '',
    version: '1',
    generate: function() {
      var plain = this.master + this.domain + this.version,
          parsed = parseInt(this.length);

      this.hashed = Sha256.hash(plain);

      if (chrome.storage && parsed !== NaN && parsed > 0) {
        var data = {};

        this.hashed = this.hashed.substring(0, parsed);

        data[this.domain] = parsed;

        chrome.storage.sync.set(data);
      }

      return this.hashed;
    },
  };

  $rootScope.$watch(function() {
    return this.domain;
  }, function (newValue) {
    if (!chrome.storage) {
      return;
    }

    chrome.storage.sync.get(this.domain, function(items) {
      var length = items[this.domain],
          parsed = parseInt(length);

      if (parsed !== NaN) {
        safeApply($rootScope, function() {
          this.length = items;
      });
      }
    });
  });

  return service;
}]);

app.controller('ExtensionController', ['$scope', 'password', function($scope, password) {
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
          safeApply($scope, function() {
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

      safeApply($scope, function() {
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