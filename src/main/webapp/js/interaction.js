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
						console.log(data);
						text = data.text[0];
						console.log(text);
						markings = data.markings[0];
						console.log(markings);
						var counter = 1;
						$("#sidebar-content").html(text);
						var content = '<div id="marking">';

						jQuery
								.each(
										markings,
										function(i, v) {

											content += '<div " id="' + counter
													+ '" ><ul>';
											content += '<h3>' + v.name
													+ '</h3>';
											content += '<l1 > Start: '
													+ v.start + '</l1></br>';
											content += '<l1> Length: '
													+ v.length + '</l1></br>';
											content += '<l1> Doc : ' + v.doc
													+ '</l1></br>';
											content += '<l1 > Uris : '
													+ '<p id= "uri">' + v.uris
													+ '</p></l1></br>';
											content += '</ul> <button onclick="removeelement('
													+ counter
													+ ')">Delete</button> </br></div>';
											counter += 1;
										});

						content += '</div>';
						content += '<script type="text/javascript"> var replaceWith = $(\'<input name="temp" type="text" />\'), connectWith = $(\'input[name="hiddenField"]\');$(\'#uri\').inlineEdit(replaceWith, connectWith);</script>'

						console.log(content);
						/* like this the results won't cummulate */
						$("#markings-list").html(content);

						// $(text).appendTo("#sidebar-content");
					}).fail(function(e) {
				// handle error
			});
};

function removeelement(divid) {
	$('#' + divid).remove();
};
function edittext(divid) {

	$("#uri" + divid).contentEditable = "true";

}
