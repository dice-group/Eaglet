/**
 * The javascript class that controls the working of the UI.
 */
$(document).ready(function() {
	$("#anotherSection").hide();
	$("#newEntity").bind("click", Selector.mouseup);

	$("#login").click(function() {
		$("#anotherSection").show();
		$("#userinfo").hide();
		$("#about").hide();
		$("#header").hide();
		$("#pipesection").hide();
		$("#loginsection").hide()
	});
});
// Basic Variables.
var counter = 1;
var documentUri;
var documentText;
var loginName;

/**
 * Method to update the document text.
 */
function updateText() {
	clearText();
	printText();
}
/**
 * Method to clean the document text.
 */
function clearText() {
	$("#sidebar-content").html("");
}
/**
 * Method to print the document text
 */
function printText() {
	var markedChars = new BitSet;
	// collect all marked characters
	$('.innerContainer .marking').each(function() {
		var start = parseInt($('.start', this).text());
		var length = parseInt($('.length', this).text());
		markedChars.setRange(start, (start + length) - 1);
	});
	var lastPos = 0;
	var oldValue = 0;
	var value;
	var markedText = "";
	for (i = 0; i < documentText.length; i++) {
		value = markedChars.get(i);
		if (value != oldValue) {
			markedText += documentText.slice(lastPos, i);
			if (oldValue == 0) {
				markedText += '<span style="font-size:14px; color:#538b01; font-weight:bold; font-style:italic;">';
			} else {
				markedText += '</span>';
			}
			lastPos = i;
		}
		oldValue = value;
	}
	markedText += documentText.slice(lastPos);
	$("#sidebar-content").html(markedText);
}
/**
 * Method to display the list of Entity.
 * 
 * @param name
 *            Name of the entity
 * @param start
 *            :The start position of the marking.
 * @param length :
 *            The length of the marking.
 * @param checkResult:
 *            The pipeline decision of the entity
 * @param uri:
 *            The uri string of the entity.
 * @param errortype:
 *            The string to store errorType
 */
function printEntity(name, start, length, checkResult, uri, errortype) {
	var content = '<div " id="' + counter
			+ '" class="marking">';
	content += '<div class="col-lg-12 text-center"> <span class="name"> <h2>'
			+ name + '</h2> </span> </div><br /> <hr/>';
	content += ' <div class="container"><div class="row"><div class="col-md-4"> Start: <span class="start">'
			+ start + '</span></div>';
	content += '<div class="col-md-4 "> Length: <span class="length">' + length
			+ '</span></div></div>';
	if(checkResult == "GOOD")
	{
		content += '<div class="row  bg-success text-white"> <div class="col-lg-4"> Result : <span class="result">'
			+ checkResult + '</span></div></div>';
	}
	else if (checkResult != null) {
		content += '<div class="row  bg-danger text-white"> <div class="col-lg-4"> Result : <span class="result">'
			+ checkResult + '</span></div></div>';
	}
	
	if (errortype != '[]') {
		content += '<div class="row bg-danger text-white"> <div class="col-md-4"> Error Type : <span id="error' + counter
				+ '" class="error">' + errortype + '</span></div></div>';
	}
	content += '<div class="row "> <div class="col-md-6">Uris : <span id="uri' + '" class="uri">  '
			+ uri + '</a></span></div> </div>';
	if (uri != null) {
		content += '<div class="row  "> <div class="col-md-4"><form name="ValidationData">'
				+ ' <div class="col-lg-20 "> <h4> Marking Decision </h4></div>'
				+ '<div class="btn-group" role="group"> <label class="btn btn-info default "> <input type="radio" class = "entityCheck" name="decision" value="correct" checked="checked">Correct</input> </label>'
				+ ' <label class="btn btn-default">  <input type="radio" class = "entityCheck" name="decision" value="wrong" >Wrong</input> </label>'
				+ ' <label class="btn btn-default"> <input type="radio" class = "entityCheck" name="decision" value="added" >Added</input> </label> '
				+ '</div></form></div></div>'
	} else {
		content += '<div class="row  "> <div class="col-lg-4"> <form name="ValidationData">'
				+ '<div class="col-lg-20 "> <h4> Marking Decision </h4> </div>'
				+ ' <div class="btn-group" role="group"> <label class="btn btn-default "> <input type="radio" class = "entityCheck" name="decision" value="correct" > Correct </input> </label>'
				+ ' <label class="btn btn-default"> <input type="radio" class = "entityCheck" name="decision" value="wrong" > Wrong </input> </label> '
				+ ' <label class="btn btn-info default"> <input type="radio" class = "entityCheck" name="decision" value="added" checked="checked"> Added </input> </label>'
				+ '</div></form></div></div>  </div>'
	}

	content += ' <br /> <div class="col-lg-2text-center"> <button type="button" class="btn btn-danger" onclick="removeelement('
			+ counter
			+ ')">Remove Entity</button> </div> </div> <br /> <br /> <hr>  ';

	$('#main-content .innerContainer').append($(content));
	counter += 1;

	$("span.uri").each(function() {
		makeUrisEditable(this);
	});
}
/**
 * The method to print the text of the document for user reference.
 * 
 * @param data:
 *            String containing text.
 */
function printDocument(data) {
	documentText = data.text[0];
	documentUri = data.uri[0];

	markings = data.markings[0];
	var lastpos = 0;
	var text_content = '';

	$("#markings-list").html('');
	$.each(markings, function(i, v) {
		printEntity(v.name, v.start, v.length, v.result, v.uris, v.error);
	});
	updateText();

	// make the URIs editable

	$("span.uri").each(function() {
		makeUrisEditable(this);
	});

}
/**
 * The method to record the user details
 */
function uservalidation() {
	// get the form data using another method
	loginName = $("input#user").val();

	$.ajax({
		url : "service/next",// servlet URL that gets
		type : "POST",// request type, can be GET
		data : {
			username : loginName,// data to be sent
		// to the
		// server
		},
		dataType : "json"// type of data returned
	}).done(printDocument).fail(function(e) {
		// handle error
		location.href = "thankyou.html";

	});
};

function configpipe() {
	// get the form data using another method
	dName = $("input#datasetname").val();
	dpath = $("input#path").val();

	$.ajax({
		url : "service/pipe",// servlet URL that gets
		type : "POST",// request type, can be GET
		data : {
			datasetname : dName,
			path : dpath,// data to be sent
		// to the
		// server
		},
		dataType : "json"// type of data returned
	}).done(location.href = "thankyou.html").fail(function(e) {
		// handle error

	})
};
/**
 * The method that makes the URI editable.
 * 
 * @param span:
 *            The span of covering the URI
 */
function makeUrisEditable(span) {
	var replaceWith = $('<input name="temp" type="text" value="" />');
	$(span).inlineEdit(replaceWith);
}
/**
 * The method to send the data back to the server.
 */

function senddata() {
	var marking_list = [];
	var attributes = {};

	$('.innerContainer .marking').each(
			function() {
				attributes = {};
				attributes["name"] = $('.name', this).text();
				attributes["length"] = $('.length', this).text();
				attributes["start"] = $('.start', this).text();
				attributes["uri"] = $('.uri', this).text();
				attributes["error"] = $('.error', this).text();
				attributes["decision"] = $("input[name='decision']:checked",
						this).val();

				marking_list.push(attributes);
			});
	$.ajax({
		url : 'service/submitResults',
		data : {
			'documenturi' : documentUri,
			'markings' : JSON.stringify(marking_list),
			'username' : loginName
		},
		type : 'POST',

	}).done(printDocument);
}
/**
 * The method to delete the id.
 * 
 * @param divid
 */
function removeelement(divid) {
	var div = $('#' + divid);
	div.next('hr').remove();
	div.remove();
	updateText();
};
/**
 * The method is utility for URI editing.
 * 
 * @param divid
 */
function edittext(divid) {

	$("#uri" + divid).contentEditable = "true";

}

// Selection of text
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
	if (st != '') {
		printEntity(st, selection.start, (selection.end - selection.start),
				null, null);
		updateText();

	}
};