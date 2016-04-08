$(document).ready(function() {
	$("#anotherSection").hide();
	$("#newEntity").bind("click", Selector.mouseup);

	$("#login").click(function() {
		$("#anotherSection").show();
		$("#userinfo").hide()
	});
});

var counter = 1;

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
						var lastpos = 0;
						var text_content = '';
						// loop for all the markings and call mark function
						$
								.each(
										markings,
										function(i, v) {
											var startpos = parseInt(v.start);
											var length = parseInt(v.length);
											var entity = text.slice(startpos,
													startpos + length);
											var rem_text = text.slice(lastpos,
													startpos);
											text_content += rem_text
													+ '<span style="font-size:14px; color:#538b01; font-weight:bold; font-style:italic;">'
													+ entity + '</span>';
											lastpos = startpos + length;
										});
						text_content += text.slice(lastpos, text.length);
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
											// content += '<l1> Doc : ' + v.doc
											// + '</l1></br>';
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

						/* like this the results won't cummulate */
						$("#markings-list").html(content);

						// $(text).appendTo("#sidebar-content");
					}).fail(function(e) {
				// handle error
			});
};
function senddata() {
	var txt = $('#markings-list').text();
}

function removeelement(divid) {
	$('#' + divid).remove();
};
function edittext(divid) {

	$("#uri" + divid).contentEditable = "true";

}

// Selection
var t = '';
Selector = {};
Selector.getSelected = function() {
	if (window.getSelection) {
		t = window.getSelection();
	} else if (document.getSelection) {
		t = document.getSelection();
	} else if (document.selection) {
		t = document.selection.createRange().text;
	}
	return t;
};

Selector.getSelectionCharOffsetsWithin = function() {
	var start = 0, end = 0;
	var sel, range, priorRange;
	var element = document.getElementById("sidebar-content");
	if (typeof window.getSelection != "undefined") {
		range = window.getSelection().getRangeAt(0);
		priorRange = range.cloneRange();
		priorRange.selectNodeContents(element);
		priorRange.setEnd(range.startContainer, range.startOffset);
		start = priorRange.toString().length;
		end = start + range.toString().length;
	} else if (typeof document.selection != "undefined"
			&& (sel = document.selection).type != "Control") {
		range = sel.createRange();
		priorRange = document.body.createTextRange();
		priorRange.moveToElementText(element);
		priorRange.setEndPoint("EndToStart", range);
		start = priorRange.text.length;
		end = start + range.text.length;
	}
	return {
		start : start,
		end : end
	};
};

Selector.mouseup = function() {
	var st = Selector.getSelected();
	var selection = Selector.getSelectionCharOffsetsWithin();
	console.log(st);
	console.log(selection);
	if (st != '') {
		var content = '<div " id="' + counter + '" ><ul>';
		content += '<a href="#"><h3>' + st + '</h3></a>';
		content += '<l1 > Start: ' + selection.start + '</l1></br>';
		content += '<l1> Length: ' + (selection.end - selection.start)
				+ '</l1></br>';
		content += '<l1 > Uris : ' + '<p id="uri' + counter
				+ '">ADD_URI</p></l1></br>';
		content += '</ul> <button onclick="removeelement(' + counter
				+ ')">Delete</button> </br></div>';
		content += '<script type="text/javascript"> var replaceWith = $(\'<input name="temp" type="text" />\'), connectWith = $(\'input[name="hiddenField"]\');$(\'p\').inlineEdit(replaceWith, connectWith);</script>';
		$('#main-content .innerContainer').append($(content));

		counter += 1;

		// $container.append([ $('<span class="ui-icon ui-icon-circle-close"
		// />')
		// .css({'display' : 'inline-block'})
		// .click(function() { $container.remove();}),
		// $('<input type="hidden" '
		// +'value="newLabel:' + st + '//' + selection.start + '//' +
		// selection.end + '" '
		// +'name="newLabel:' + st + '//' + selection.start + '//' +
		// selection.end + '"/>'),
		// $('<span>' + st + '</span><br />') ]);
	}
};
