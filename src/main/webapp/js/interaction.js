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

						$.each(markings, function(i, v) {

							content += '<div " id="' + counter
									+ '" class="marking"><ul>';
							content += '<a href="#">' + '<span class="name">'
									+ v.name + '</span></a>';
							content += '<l1 > Start: ' + '<span class="start">'
									+ v.start + '</span></l1></br>';
							content += '<l1> Length: '
									+ '<span class="length">' + v.length
									+ '</span></l1></br>';
							content += '<l1> Result : '
									+ '<span class="result">' + v.result
									+ '</span></l1></br>';
							content += '<l1 > Uris : ' + '<span id="uri'
									+ counter + '" class="uri">' + v.uris
									+ '</span></l1></br>';
							content += '</ul> <button onclick="removeelement('
									+ counter
									+ ')">Delete</button> </br></div>';
							counter += 1;
						});

						content += '</div>';

						/* like this the results won't cummulate */
						$("#markings-list").html(content);
						// make the URIs editable
						makeUrisEditable();

						// $(text).appendTo("#sidebar-content");
					}).fail(function(e) {
				// handle error
			});
};

function makeUrisEditable() {
	var replaceWith = $('<input name="temp" type="text" value="" />');
	$('span.uri').inlineEdit(replaceWith);
}

function senddata() {
	var content_html = $('#markings-list').html();
	var marking_list = [];

	$('.innerContainer').each(function() {
		var attributes = {};
		$('.marking', this).each(function() {

			attributes["name"] = $('.name', this).text();
			attributes["length"] = $('.length', this).text();

			attributes["start"] = $('.start', this).text();
			attributes["result"] = $('.result', this).text();
			attributes["uri"] = $('.uri', this).text();

		});
		marking_list.push(attributes);
	});
	Console.log(marking_list);
	$.ajax({
		url : '/submitResults',
		data : {
			'string' : marking_list
		},
		type : 'GET',
		cache : false,
	}).done(function(result) {
		Console.log("Sent")
	});
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
		content += '<a href="#"><span class="name"><h3>' + st
				+ '</h3></span></a>';
		content += '<l1 >  Start: ' + '<span class="start">' + selection.start
				+ '</span></l1></br>';
		content += '<l1> Length: ' + '<span class="length">'
				+ (selection.end - selection.start) + '</span></l1></br>';
		content += '<l1 > Uris : ' + '<span class="uri" id="uri' + counter
				+ '">ADD_URI</span></l1></br>';
		content += '</ul> <button onclick="removeelement(' + counter
				+ ')">Delete</button> </br></div>';
		$('#main-content .innerContainer').append($(content));

		counter += 1;
		makeUrisEditable();

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
