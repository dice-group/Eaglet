function uservalidation() {
	// get the form data using another method
	var loginName = $("input#user").val();

	$
			.ajax({
				url : "service/next",// servlet URL that gets first option as
				// parameter and returns JSON of to-be-populated
				// options
				type : "GET",// request type, can be GET
				data : {
					username : loginName,// data to be sent to the server
				},
				dataType : "json"// type of data returned
			})
			.done(
					function(data) {
						if (data.success) {
							var json_x = JSON.parse(data);
							text = json_x.text;
							markings = json_x.markings;
							var counter = 1;
							$("#sidebar-content").html = generateText(text,
									markings);
							content += '<div id="accordion">';
							jQuery
									.each(
											markings,
											function(i, v) {
												content += '<h3>' + v.name
														+ '</h3>';
												content += '<div onclick="dropdown()" id=' + counter
														+ 'on><ul>';
												content += '<l1> Start:'
														+ v.start + '</l1>';
												content += '<l1> Length:'
														+ v.length + '</l1>';
												content += '<l1> Start:'
														+ v.doc + '</l1>';
												content += '<l1> Uris::'
														+ v.uris + '</l1>';
												content += '</ul> <button onclick="removeelement(counter)">Delete</button> </br>'
													+'<button onclick="edittext()">Edit</button> </div>';
												counter += 1;
											});

							content += '</div>';
							/* like this the results won't cummulate */
							jQuery("#OTHER_ENTITY_CANDIDATE_ID").html(content);

							// $(text).appendTo("#sidebar-content");
						}
					}).fail(function(e) {
				// handle error
			});
};
function removeelement(divid) {
	$('#' + divid)
	remove();
};
function edittext() {
	$('#username').editable({
	    type: 'text',
	    url: '/post',    
	    pk: 1,    
	    title: 'Enter Text',
	    ajaxOptions: {
	        type: 'put'
	    }        
	});

	//ajax emulation
	$.mockjax({
	    url: '/post',
	    responseTime: 200,
	    response: function(settings) {
	        console.log(settings);
	    }
	}); 

}


function dropdown () {
	$("#accordion").accordion({
		event : "click hoverintent"
	});
};
$.event.special.hoverintent = {
	setup : function() {
		$(this).bind("mouseover", jQuery.event.special.hoverintent.handler);
	},
	teardown : function() {
		$(this).unbind("mouseover", jQuery.event.special.hoverintent.handler);
	},
	handler : function(event) {
		var currentX, currentY, timeout, args = arguments, target = $(event.target), previousX = event.pageX, previousY = event.pageY;

		function track(event) {
			currentX = event.pageX;
			currentY = event.pageY;
		}
		;

		function clear() {
			target.unbind("mousemove", track).unbind("mouseout", clear);
			clearTimeout(timeout);
		}

		function handler() {
			var prop, orig = event;

			if ((Math.abs(previousX - currentX) + Math
					.abs(previousY - currentY)) < 7) {
				clear();

				event = $.Event("hoverintent");
				for (prop in orig) {
					if (!(prop in event)) {
						event[prop] = orig[prop];
					}
				}

				delete event.originalEvent;

				target.trigger(event);
			} else {
				previousX = currentX;
				previousY = currentY;
				timeout = setTimeout(handler, 100);
			}
		}

		timeout = setTimeout(handler, 100);
		target.bind({
			mousemove : track,
			mouseout : clear
		});
	}
};
