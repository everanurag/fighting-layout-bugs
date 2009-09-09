/**
 * Javascript functions for esprit.com
 *
 * @author mario@wysiwyg.de
 * http://www.wysiwyg.de
 */




/**
 * Topnavigation layer ie6
 *
 */
sfHover = function()
{
	if (document.getElementById("navigation")) {
		var sfEls = document.getElementById("navigation").getElementsByTagName("LI");
		for (var i=0; i<sfEls.length; i++) {
			sfEls[i].onmouseover=function() {
				this.className+=" sfhover";
			}
			sfEls[i].onmouseout=function() {
				this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
			}
		}
	}
}

if (window.attachEvent) window.attachEvent("onload", sfHover);

function showHideDropDown () {
	var box = document.getElementById('dropdownbox');
	box.style.display = box.style.display == "block" ? "none" : "block";
}



/**
 * Open Help-popup
 *
 * @param page_id for which help is shown
 */
function showHelp(param)
{
	var url;
	url = "help.php?page="+param;
	window.open(url,"help","width=550,height=450,status=1");
}

/**
 * Submit a form if a field is not empty
 *
 * @param id_field id of the field to check
 * @param id_form  id of the form to submit
 */
function submitIfFilled(id_field, id_form)
{
	var elem_to_check = document.getElementById(id_field);

	if (elem_to_check != null) {
		if (elem_to_check.value != null && elem_to_check.value.length > 0) {
			document.forms[id_form].submit();
		}
		else {
			alert("Please fill in the required field(s)");
		}
	}
}
/**
 * Submit a form if a radiobutton is chosen
 *
 * @param id_field id of the field to check
 * @param id_form  id of the form to submit
 */
function submitIfSelected(id_field, id_form)
{
	var num_elements = document.forms[id_form].elements.length;

	for (i=0; i<num_elements; i++) {
		if (document.forms[id_form].elements[i].type == "radio") {
			if (document.forms[id_form].elements[i].checked == true) {
				document.forms[id_form].submit();
				return;
			}
		}
	}
	alert("Please choose an option");
}

function setFormValSubmit(form, field, value)
{
	document.forms[form].elements[field].value = value;
	document.forms[form].submit();
}

function checkDates(from, until, submit)
{
	var from_day = document.getElementById(from+"[day]");
	var from_month = document.getElementById(from+"[month]");
	var from_year = document.getElementById(from+"[year]");

	var until_day = document.getElementById(until+"[day]");
	var until_month = document.getElementById(until+"[month]");
	var until_year = document.getElementById(until+"[year]");

	var dfrom = new Date(from_year.value, from_month.value-1, from_day.value);
	var duntil = new Date(until_year.value, until_month.value-1, until_day.value);

	var dnow = new Date();

	var submitBtn = document.getElementById(submit);

	if (dfrom.getTime() > duntil.getTime())	{
		alert("Timespan: The date 'from' should be before the date in 'until'.");
		submitBtn.style.visibility = "hidden";
	}

	else if (dfrom.getTime() > dnow.getTime() || duntil.getTime() > dnow.getTime()) {
		alert("Timespan: I'm afraid I can not predict the future - please choose a past date.");
		submitBtn.style.visibility = "hidden";
	}
	else {
		submitBtn.style.visibility = "visible";
	}
}

function statReportNavi(hide_time, hide_area)
{
	var sel = document.getElementById("stat_report");
	if (sel != null && sel.value != null) {
		if (hide_time.indexOf(sel.value) != -1) {
			hideElemById("stat_timespan");
		}
		else {
			showElemById("stat_timespan");
		}

		if (hide_area.indexOf(sel.value) != -1) {
			hideElemById("stat_area");
		}
		else {
			showElemById("stat_area");
		}
	}
}

/**
 * Traverse all p-tags whose id starts with 'faq_', hide them, except
 * the id == 'faq_'+to_show
 *
 * @param to_show which P-tag should be shown?
 */
function showAndHideFAQ(to_show)
{
	elems = document.getElementsByTagName("P");
	for (i = 0; i < elems.length; ++i) {
		if (elems[i] != null) {
			idvalue = elems[i].id;
			if (idvalue!= null) {
				var id = idvalue;
				if (id.substring(0,4) == 'faq_') {
					var faq_id = id.substring(4);
					if (faq_id == to_show) {
						showElemById(id);
					}
					else {
						hideElemById(id);
					}
				}
			}
		}
	}
}

function hideElemById(id)
{
	elem = document.getElementById(id);
	if (elem != null) {
		elem.style.visibility = "hidden";
		elem.style.display = "none";
	}
}

function showElemById(id)
{
	elem = document.getElementById(id);
	if (elem != null) {
		elem.style.visibility = "visible";
		elem.style.display = "block";
	}
}

function focusFormField(id_field)
{
	var elem = document.getElementById(id_field);
	if (elem != null) {
		elem.focus();
	}

}

function enableCheckbox(id_field)
{
	var elem = document.getElementById(id_field);
	if (elem != null) {
		elem.setAttribute('checked', 1);
	}
}

function alertOrSubmit(form_name, field_list, error_msg)
{
	var ok = false;
	var name_array = field_list.split(',');
	if (name_array != null) {
		for (var i=0; i<name_array.length; i++) {
			if (document.forms[form_name].elements[name_array[i]].checked == true) {
				ok = true;
			}
		}
	}
	if (!ok) {
		alert(error_msg);
	}
	else {
		document.forms[form_name].submit();
	}
}

function entsub(event, form)
{
	if (event && event.which == 13)
		form.submit();
	else
		return true;
}

function entsubradius(event)
{
	if (window.event && window.event.keyCode == 13) {
		//alert("go");
		submitRadius()
	}
	else {
		return true;
	}
}

function writeDocument(content)
{
	document.write(content);
}