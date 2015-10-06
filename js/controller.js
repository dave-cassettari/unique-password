$(function() {
  function AppViewModel() {
    var DEFAULT_LENGTH = null,
        DEFAULT_INCLUDE = '!"£$%^&*():@~<>?',
        INCLUDE_COUNT = 4,
        self = this,
        tab = null,
        isChrome = (typeof chrome !== 'undefined');

    function replaceAt(string, index, character) {
      return string.substr(0, index) + character + string.substr(index + character.length);
    }

    function saveSettings() {
      if (isChrome && chrome.storage !== undefined) {
        var key = Sha256.hash(self.domain()),
            data = {};

        if (self.length() !== DEFAULT_LENGTH) {
          data.length = self.length();
        }

        if (self.include() !== DEFAULT_INCLUDE) {
          data.include = self.include();
        }

        chrome.storage.sync.set(data);
      }
    }

    function loadSettings() {
      if (isChrome && chrome.storage !== undefined) {
        var key = Sha256.hash(self.domain());

        chrome.storage.sync.get(key, function(data) {
          if (data.hasOwnProperty('length')) {
            self.length(data.length);
          }

          if (data.hasOwnProperty('include')) {
            self.include(data.include);
          }
        });
      }
    }

    if (isChrome && chrome.tabs !== undefined) {
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
    self.length = ko.observable(DEFAULT_LENGTH);
    self.version = ko.observable(1);
    self.options = ko.observable(false);
    self.include = ko.observable(DEFAULT_INCLUDE);

    self.hashed = ko.pureComputed(function() {
      var master = self.master(),
          domain = self.domain(),
          version = self.version(),
          include = self.include(),
          plain = master + domain + version,
          length = parseInt(self.length()),
          hashed = Sha256.hash(plain);

      if (master === '' || domain ==='') {
        return '';
      }

      if (length !== NaN && length > 0) {
        var data = {},
            key = Sha256.hash(self.domain());

        hashed = hashed.substring(0, length);

        data[key] = length;
      }

      if (include !== null && include !== '') {
        var i,
            offset = 0,
            frequency = 0,
            hashedCode = 0,
            includeChars = include.split('');

        includeChars.sort(function(a, b){
          if(a.charCodeAt(0) < b.charCodeAt(0)) return -1;
          if(a.charCodeAt(0) > b.charCodeAt(0)) return 1;

          return 0;
        });

        for (i = 0; i < hashed.length; i++)
        {
          hashedCode += hashed.charCodeAt(i);
        }

        offset = hashedCode % include.length;
        frequency = Math.floor(hashed.length / INCLUDE_COUNT)

        for (i = 0; i < INCLUDE_COUNT; i++)
        {
          var includeChar = includeChars[(offset + i) % include.length],
              includeIndex = (offset + frequency * i) % hashed.length;

          hashed = replaceAt(hashed, includeIndex, includeChar);
        }
      }

      saveSettings();

      return hashed;
    });

    self.discover = function() {
      if (isChrome && chrome.tabs !== undefined) {
        chrome.tabs.sendMessage(tab.id, { id: 'find-input', reset: false });
      }
    };

    self.populate = function() {
      if (isChrome && chrome.tabs !== undefined) {
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
      return !(isChrome && chrome.tabs);
    };

    self.domain.subscribe(function(newValue) {
      loadSettings();
    });
  };

  ko.applyBindings(AppViewModel);
});