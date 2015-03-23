(function()
{
	var CLASS_NAME = 'password-generator-active';

	chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
		console.log(request);

		var $inputs = $('input[type="password"]'),
			$active = $inputs.filter('.' + CLASS_NAME);

		switch (request.id) {
			case 'find-input':
				var index = 0;

				if (request.reset === false && $active.length > 0) {
					$active.removeClass(CLASS_NAME);

					index = ($inputs.index($active) + 1) % $inputs.length;
				}

				$inputs.eq(index).addClass(CLASS_NAME);
				
				break;

			case 'set-value':
				var $selected = null;

				if ($active.length > 0) {
					$selected = $active.first();
				} else {
					$selected = $inputs.first();
				}

				if ($selected.length > 0) {
					$selected
						.addClass(CLASS_NAME)
						.val(request.value);
				}
				break;
		}
	});
})();