$(document).ready(function() {
	$("#anotherSection").hide();

	$("#login").click(function() {
		$("#anotherSection").show();
		$("#userinfo").hide()
	});
});
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

						markings = data.markings[0];
						console.log(markings);
						var counter = 1;
						var lastpos = 0;
						var text_content = '<p>';
						// loop for all the markings and call mark function
						$
								.each(
										markings,
										function(i, v) {
											var startpos = v.start;
											var length = v.length;
											var entity = text.slice(startpos,
													startpos + length);
											var rem_text = text.slice(lastpos,
													startpos);
											text_content += rem_text
													+ '<p style="font-size:14px; color:#538b01; font-weight:bold; font-style:italic;">'
													+ entity + '</p>';
											lastpos = startpos + length;
										});
						text_content += '</p>';
						$("#sidebar-content").html(text_content);
						var content = '<div id="marking">';

						$
								.each(
										markings,
										function(i, v) {

											content += '<div " id="' + counter
													+ '" ><ul>';
											content += '<a href="#"><h3>'
													+ v.name + '</h3></a>';
											content += '<l1 > Start: '
													+ v.start + '</l1></br>';
											content += '<l1> Length: '
													+ v.length + '</l1></br>';
											content += '<l1> Doc : ' + v.doc
													+ '</l1></br>';
											content += '<l1> Result : '
													+ v.result + '</l1></br>';
											content += '<l1 > Uris : '
													+ '<p id="uri' + counter
													+ '">' + v.uris
													+ '</p></l1></br>';
											content += '</ul> <button onclick="removeelement('
													+ counter
													+ ')">Delete</button> </br></div>';
											counter += 1;
										});

						content += '</div>';
						content += '<script type="text/javascript"> var replaceWith = $(\'<input name="temp" type="text" />\'), connectWith = $(\'input[name="hiddenField"]\');$(\'p\').inlineEdit(replaceWith, connectWith);</script>'

						console.log(content);
						/* like this the results won't cummulate */
						$("#markings-list").html(content);

						// $(text).appendTo("#sidebar-content");
					}).fail(function(e) {
				// handle error
			});
};
function senddata() {
	var txt = $('#markings-list').text();
	console.log(text);
}

function removeelement(divid) {
	$('#' + divid).remove();
};
function edittext(divid) {

	$("#uri" + divid).contentEditable = "true";

}
