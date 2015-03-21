(function()
{
	var i,
		inputs = document.querySelectorAll('input'),
		timeout = null,
		selected = null;

	for (i = 0; i < inputs.length; i++)
	{
		inputs[i].addEventListener('focus', function() {
			if (this.type == 'password') {
				selected = this;
			}
		}, true);

		inputs[i].addEventListener('blur', function() {
			var input = this;

			timeout = setTimeout(function() {
				if (input.type == 'password') {
					selected = null;
				}
			}, 1000);

			
		}, true);
	}

	chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
		switch (request.id) {
			case 'is-opening':
				if (timeout != null) {
					clearTimeout(timeout);
				}
				break;

			case 'set-password':
				if (selected == null) {
					sendResponse({ success: false });
				} else {
					selected.value = request.password;

					sendResponse({ success: true });
				}
				console.log(selected == null);
				break;
		}
	});
})();