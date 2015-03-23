$(function() {
  function AppViewModel() {
    var self = this,
        tab = null;

    if (chrome && chrome.tabs) {
      chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
        tab = tabs[0];

        var url = new URL(tab.url);

        self.domain(url.hostname);

        chrome.tabs.sendMessage(tab.id, { id: 'find-input', reset: true });
      });
    }

    self.reveal = ko.observable(false);
    self.domain = ko.observable('');
    self.master = ko.observable('');
    self.length = ko.observable(null);
    self.version = ko.observable(1);
    self.options = ko.observable(false);

    self.hashed = ko.pureComputed(function() {
      var plain = self.master() + self.domain() + self.version(),
          length = parseInt(self.length()),
          hashed = Sha256.hash(plain);

      if (chrome.storage && length !== NaN && length > 0) {
        var data = {},
            key = Sha256.hash(self.domain());

        hashed = hashed.substring(0, length);

        data[key] = length;

        chrome.storage.sync.set(data);
      }

      return hashed;
    });

    self.discover = function() {
      if (chrome && chrome.tabs) {
        chrome.tabs.sendMessage(tab.id, { id: 'find-input', reset: false });
      }
    };

    self.populate = function() {
      if (chrome && chrome.tabs) {
        chrome.tabs.sendMessage(tab.id, { id: 'set-value', value: self.hashed() });
      }
    };

    self.toggleReveal = function() {
      self.reveal(!self.reveal());

      if (self.reveal()) {
        $('#input-hashed').focus().select();
      } else {
        $('#input-master').focus();
      }
    };

    self.toggleOptions = function() {
      self.options(!self.options());
    };

    self.isApplication = function() {
      console.log(chrome.tabs);
      return !(chrome && chrome.tabs);
    };

    self.domain.subscribe(function(newValue) {
      var key = Sha256.hash(newValue);

      if (chrome && chrome.storage) {
        chrome.storage.sync.get(key, function(items) {
          var value = items[key],
              length = parseInt(value);

          if (length !== NaN) {
            self.length(length);;
          }
        });
      }
    });
  };

  ko.applyBindings(AppViewModel);
});