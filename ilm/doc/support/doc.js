<!-- Begin

// ------------------------------
// Interbook Hyperlink Management
// ------------------------------

var refmanTOC = "Dispatcher31RefTOC"
var usermanTOC = "Dispatcher31UsrTOC"
var getstartTOC = "Dispatcher31GSTOC"


// manualType = "refman" or "userman" or "getstart"
// manualTOC = refmanTOC or usermanTOC or getstartTOC
// manualWindow = "refwin" or "userwin" or "gswin"
// htmlPage is the destination of the hypertext link:
//     filename.html#anchorname or filename.html
function loadManual(htmlPage, manualType, manualTOC, manualWindow) {
   framecode = 
      "<frameset rows='80,*'  bordercolor='black' border=2>"
      + "<frame src='../../"
      + manualType
      + "/onlinedoc/mantitle.html' name='ManualTitle' scrolling=no>"
      + "<frameset cols='300, *' bordercolor='black'>"
      + "<frameset rows='0,*' bordercolor=black>"
      + "<frame src='../../"
      + manualType
      + "/html/"
      + manualTOC
      + ".html' name='code' scrolling='no' border='0'>"
      + "<frame src='../onlinedoc/emptyTOC.html' NAME='ManualContents'>"
      + "</frameset>"
      + "<frame src='../../"
      + manualType
      + "/html/" 
      + htmlPage
      + "' name='ManualBody'>"
      + "</frameset>"
      + "</frameset>";

   page = window.open("", manualWindow);
   page.document.open();
   page.document.write(framecode);
   page.document.close();
   page.focus();
}

function loadRef(htmlPage) {
   loadManual(htmlPage, "refman", refmanTOC, "refwin");
}

function loadUsr(htmlPage) {
   loadManual(htmlPage, "userman", usermanTOC, "userwin");
}

function loadGs(htmlPage) {
   loadManual(htmlPage, "getstart", getstartTOC, "gswin");
}

// ------------------------
// Combo Box Functions
// ------------------------

function gotomanual(form)
{
var manual=form.othermanuals.options[form.othermanuals.selectedIndex].value;
  if (manual == "refman") {  
    newWindow = windowOpen('../../refman/onlinedoc/index.html', 'refwin');
  }
  if (manual == "userman") {  
    newWindow = windowOpen('../../userman/onlinedoc/index.html', 'userwin');
  }
  if (manual == "relnotes") {  
    newWindow = windowOpen('../../relnotes/onlinedoc/index.html', 'relwin');
  }
  if (manual == "getstart") {  
    newWindow = windowOpen('../../getstart/onlinedoc/index.html', 'gswin');
  }
}


// ------------------------
// Window Opening Mechanism
// ------------------------

if (document.all) {
  x = window.screenLeft;
  y = window.screenTop;
}
else {
  x = window.screenX;
  y = window.screenY;
}

function windowOpen(filename,windowname) {
  newWindow = window.open(filename,windowname);
  newWindow.focus();
}

//------------
// windowAdd is similar to windowOpen, but doesn't close the
// opener window.
// Used for example to open the search window without closing the 
// manual window.
// -----------
function windowAdd(filename,windowname) {
  newWindow = window.open(filename,windowname,'top=' + y + ',screenY=' + y + ',left=' + x + ',screenX=' + x + ',menubar=yes,scrollbars=yes,toolbar=yes,status=yes,resizable=yes');
  if (window.focus) {
    newWindow.focus();
  }
}

function setWindowLoc() {
  if (document.all) {
    x = window.screenLeft;
    y = window.screenTop;
  }
  else {
    x = window.screenX;
    y = window.screenY;
  }
}

function checkMove () {
  curScreenX = 
    document.all ? window.screenLeft : window.screenX;
  curScreenY = 
    document.all ? window.screenTop : window.screenY;
  if (curScreenX != oldScreenX || curScreenY != oldScreenY) {
    window.setWindowLoc();
  }
  oldScreenX = curScreenX;
  oldScreenY = curScreenY;
}

// -------------------------
// Navigation bar management
// -------------------------

// Based on watermark script by Paul Anderson, CNET Builder.com. All rights reserved.

function initNavBar() {
markW = 100;       // pixels wide
markH = 40;       // pixels high
markX = 99;      // percent right
markY = 90;      // percent down
markRefresh = 20; // milliseconds

// set common object reference
if (!document.all) document.all = document;
if (!document.all.waterMark.style) document.all.waterMark.style = document.all.waterMark;

wMark = document.all.waterMark.style;
wMark.width = markW;
wMark.height = markH;
navDOM = window.innerHeight; // Nav DOM flag
}
 
function setVals() {
 barW = 0; // scrollbar compensation for PC Nav
 barH = 0;
 if (navDOM) {
  if (document.height > innerHeight) barW = 20;
  if (document.width > innerWidth) barH = 20;
  } else {
  innerWidth = document.body.clientWidth;
  innerHeight = document.body.clientHeight;
  }
 posX = ((innerWidth - markW)-barW) * (markX/100);
 posY = ((innerHeight - markH)-barH) * (markY/100);
 }

function wRefresh() {
 wMark.left = posX + (navDOM?pageXOffset:document.body.scrollLeft);
 wMark.top = posY + (navDOM?pageYOffset:document.body.scrollTop);
 }

function markMe() {
 setVals();
 window.onresize=setVals;
 markID = setInterval ("wRefresh()",markRefresh);
 }

// ----------------------
// Menu Tracking Function
// ----------------------

function setMenuTracking() {
  wfami = typeof parent.frames["code"];
  if (wfami != "undefined") {
    if(parent.frames['code'].MTMDisplayMenu != null) {
      parent.frames['code'].MTMTrack = true;
      setTimeout("parent.frames['code'].MTMDisplayMenu()", 250);
    }
  }
}

// --------------------------
// Suppress JavaScript Errors
// --------------------------

function stopError() {
  return true;
}



//  End -->

