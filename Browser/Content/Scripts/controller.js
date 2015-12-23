$(function () {
    function AppViewModel() {
        var DEFAULT_LENGTH = null,
            DEFAULT_INCLUDE = '!"£$%^&*():@~<>?',
            INCLUDE_COUNT = 4,
            MAX_RECENTS = 5,
            KEY_SETTINGS = '_app',
            self = this,
            tab = null,
            isChrome = (typeof chrome !== 'undefined'),
            hasStorage = ('localStorage' in window && window['localStorage'] !== null);

        function replaceAt(string, index, character) {
            return string.substr(0, index) + character + string.substr(index + character.length);
        }

        function save(key, data) {
            if (isChrome && chrome.storage !== undefined) {
                var chromeData = {};

                chromeData[key] = data;

                chrome.storage.sync.set(chromeData);
            } else if (hasStorage) {
                localStorage.setItem(key, JSON.stringify(data));
            }
        }

        function load(key, callback) {
            if (isChrome && chrome.storage !== undefined) {
                chrome.storage.sync.get(key, function (response) {
                    var data = response[key];

                    if (data) {
                        callback(data);
                    }
                });
            } else if (hasStorage) {
                var data = JSON.parse(localStorage.getItem(key));

                if (data) {
                    callback(data);
                }
            }
        }

        function saveSettings() {
            var key = Sha256.hash(self.domainKey()),
                data = {};

            if (self.length() !== DEFAULT_LENGTH) {
                data.length = self.length();
            }

            if (self.include() !== DEFAULT_INCLUDE) {
                data.include = self.include();
            }

            save(key, data);
        }

        function loadSettings() {
            var key = Sha256.hash(self.domainKey()),
                callback;

            callback = function (data) {
                if (data.hasOwnProperty('length')) {
                    self.length(data.length);
                } else {
                    self.length(DEFAULT_LENGTH);
                }

                if (data.hasOwnProperty('include')) {
                    self.include(data.include);
                } else {
                    self.include(DEFAULT_INCLUDE);
                }
            };

            load(key, callback);
        }

        if (isChrome && chrome.tabs !== undefined) {
            chrome.tabs.query({ active: true, currentWindow: true }, function (tabs) {
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
        self.recents = ko.observableArray();
        self.recentShow = ko.observable(false);

        self.recentSet = function (newDomain) {
            self.domain(newDomain);
            self.recentShow(false);
        };

        self.recentRemove = function (oldDomain) {
            self.recents.remove(oldDomain);
        };

        self.recentToggle = function () {
            self.recentShow(!self.recentShow());
        };

        self.domainKey = ko.pureComputed(function () {
            var domain = self.domain();

            if (domain === null || domain === undefined || domain === '') {
                return null;
            }

            return domain.trim().toLowerCase();
        }).extend({ throttle: 500 });

        self.hashed = ko.pureComputed(function () {
            var master = self.master(),
                domain = self.domainKey(),
                version = self.version(),
                include = self.include(),
                plain = master + domain + version,
                length = parseInt(self.length()),
                hashed = Sha256.hash(plain);

            if (master === '' || domain === null) {
                return '';
            }

            if (length !== NaN && length > 0) {
                var data = {},
                    key = Sha256.hash(domain);

                hashed = hashed.substring(0, length);

                data[key] = length;
            }

            if (include !== null && include !== '') {
                var i,
                    offset = 0,
                    frequency = 0,
                    hashedCode = 0,
                    includeChars = include.trim().split('');

                includeChars.sort(function (a, b) {
                    if (a.charCodeAt(0) < b.charCodeAt(0)) return -1;
                    if (a.charCodeAt(0) > b.charCodeAt(0)) return 1;

                    return 0;
                });

                for (i = 0; i < hashed.length; i++) {
                    hashedCode += hashed.charCodeAt(i);
                }

                offset = hashedCode % include.length;
                frequency = Math.floor(hashed.length / INCLUDE_COUNT)

                for (i = 0; i < INCLUDE_COUNT; i++) {
                    var includeChar = includeChars[(offset + i) % include.length],
                        includeIndex = (offset + frequency * i) % hashed.length;

                    hashed = replaceAt(hashed, includeIndex, includeChar);
                }
            }

            saveSettings();

            return hashed;
        });

        self.discover = function () {
            if (isChrome && chrome.tabs !== undefined) {
                chrome.tabs.sendMessage(tab.id, { id: 'find-input', reset: false });
            }
        };

        self.populate = function () {
            if (isChrome && chrome.tabs !== undefined) {
                chrome.tabs.sendMessage(tab.id, { id: 'set-value', value: self.hashed() });
            }
        };

        self.toggleReveal = function () {
            self.reveal(!self.reveal());

            if (self.reveal()) {
                $('#input-hashed').focus().select();
            } else {
                $('#input-master').focus();
            }
        };

        self.toggleOptions = function () {
            self.options(!self.options());
        };

        self.isApplication = function () {
            return !(isChrome && chrome.tabs);
        };

        self.domainKey.subscribe(function (newValue) {
            loadSettings();

            var recentIndex = self.recents().indexOf(newValue);

            if (recentIndex >= 0) {
                self.recents().splice(recentIndex, 1);
            } else if (self.recents().length >= MAX_RECENTS) {
                self.recents().pop();
            }

            self.recents.unshift(newValue);
        });

        self.recents.subscribe(function (newValue) {
            var data = {
                recents: self.recents(),
            };

            save(KEY_SETTINGS, data);
        });

        load(KEY_SETTINGS, function (data) {
            if (data.hasOwnProperty('recents')) {
                self.recents(data.recents);
            }
        });
    };

    ko.applyBindings(AppViewModel);
});