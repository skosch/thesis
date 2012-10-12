<!-- Begin

// Based on Morten's JavaScript Tree Menu
// written by Morten Wang <warnckew@online.no> (c) 1998-1999

/******************************************************************************
* Define the MenuItem object.                                                 *
******************************************************************************/
function MTMenuItem(text, url, target, icon) {
  this.text = text;
  this.url = url ? url : "";
  this.target =  target ? target : "";
  this.icon = icon ? icon : "";

  this.number = MTMSubNumber++;

  this.submenu     = null;
  this.expanded    = false;
  this.MTMakeSubmenu = MTMakeSubmenu;
}

function MTMakeSubmenu(menu) {
  this.submenu = menu;
}

/******************************************************************************
* Define the Menu object.                                                     *
******************************************************************************/

function MTMenu() {
  this.items   = new Array();
  this.MTMAddItem = MTMAddItem;
}

function MTMAddItem(item) {
  this.items[this.items.length] = item;
}

/******************************************************************************
* Define the icon list, addIcon function and MTMIcon item.                    *
******************************************************************************/

function IconList() {
  this.items = new Array();
  this.addIcon = addIcon;
}

function addIcon(item) {
  this.items[this.items.length] = item;
}

function MTMIcon(iconfile, match, type) {
  this.file = iconfile;
  this.match = match;
  this.type = type;
}

/******************************************************************************
* Global variables.  Not to be altered unless you know what you're doing.     *
* User-configurable options are at the end of this document.                  *
******************************************************************************/

var MTMLevel;
var MTMBar = new Array();
var MTMIndices = new Array();
var MTMBrowser = null;
var MTMNN3 = false;
var MTMNN4 = false;
var MTMIE4 = false;
var MTMUseStyle = true;

if(navigator.appName == "Netscape") {
  if(parseInt(navigator.appVersion) == 3 && (navigator.userAgent.indexOf("Opera") == -1)) {
    MTMBrowser = true;
    MTMNN3 = true;
    MTMUseStyle = false;
  } else if(parseInt(navigator.appVersion) >= 4) {
    MTMBrowser = true;
    MTMNN4 = true;
  }
} else if (navigator.appName == "Microsoft Internet Explorer" && parseInt(navigator.appVersion) >= 4) {
  MTMBrowser = true;
  MTMIE4 = true;
}

var MTMClickedItem = false;
var MTMExpansion = false;

var MTMSubNumber = 1;
var MTMTrackedItem = false;
var MTMTrack = false;

var MTMPreHREF = "";
if(MTMIE4 || MTMNN3) {
  MTMPreHREF += document.location.href.substring(0, document.location.href.lastIndexOf("/") +1);
}

var MTMFirstRun = true;
var MTMCurrentTime = 0; // for checking timeout.
var MTMUpdating = false;
var MTMSubName = "";
var MTMWinSize, MTMyval;
var MTMOutputString = "";

/******************************************************************************
* Code that picks up frame names of frames in the parent frameset.            *
******************************************************************************/

if(MTMBrowser) {
  var MTMFrameNames = new Array();
  for(i = 0; i < parent.frames.length; i++) {
    MTMFrameNames[i] = parent.frames[i].name;
  }
}

/******************************************************************************
* Dummy function for sub-menus without URLs                                   *
* Thanks to Michel Plungjan for the advice. :)                                *
******************************************************************************/

function myVoid() { ; }

/******************************************************************************
* Functions to draw the menu.                                                 *
******************************************************************************/

function MTMSubAction(SubItem, ReturnValue) {

  SubItem.expanded = (SubItem.expanded) ? false : true;
  if(SubItem.expanded) {
    MTMExpansion = true;
  }

  MTMClickedItem = SubItem.number;

  if(MTMTrackedItem && MTMTrackedItem != SubItem.number) {
    MTMTrackedItem = false;
  }

  if(!ReturnValue) {
    setTimeout("MTMDisplayMenu()", 10);
  }

  return ReturnValue;
}

function MTMStartMenu() {
  if(MTMFirstRun) {
    MTMCurrentTime++;
    if(MTMCurrentTime == MTMTimeOut) { // call MTMDisplayMenu
      setTimeout("MTMDisplayMenu()",10);
    } else {
      setTimeout("MTMStartMenu()",100);
    }
  } 
}

function MTMDisplayMenu() {
  if(MTMBrowser && !MTMUpdating) {
    MTMUpdating = true;
    MTMFirstRun = false;

    if(MTMTrack) {
      MTMTrackedItem = MTMTrackExpand(menu);
      if(MTMExpansion && MTMSubsAutoClose) {
        MTMCloseSubs(menu);
      }
    } else if(MTMExpansion && MTMSubsAutoClose) {
      MTMCloseSubs(menu);
    }

    MTMLevel = 0;
    MTMDoc = parent.frames[MTMenuFrame].document
    MTMDoc.open("text/html", "replace");
    MTMOutputString = '<html><head>';
    if(MTMLinkedSS) {
      MTMOutputString += '<link rel="stylesheet" type="text/css" href="' + MTMPreHREF + MTMSSHREF + '">';
    } else if(MTMUseStyle) {
      MTMOutputString += '<style type="text/css">body {color:' + MTMTextColor + ';background:';
      MTMOutputString += (MTMBackground == "") ? MTMBGColor : MTMakeBackImage(MTMBackground);
      MTMOutputString += ';} #root {color:' + MTMRootColor + ';background:' + ((MTMBackground == "") ? MTMBGColor : 'transparent') + ';font-family:' + MTMRootFont + ';font-size:' + MTMRootCSSize + ';} ';
      MTMOutputString += 'a {font-family:' + MTMenuFont + ';font-size:' + MTMenuCSSize + ';text-decoration:none;color:' + MTMLinkColor + ';background:' + MTMakeBackground() + ';} ';
      MTMOutputString += MTMakeA('pseudo', 'hover', MTMAhoverColor);
      MTMOutputString += MTMakeA('class', 'tracked', MTMTrackColor);
      MTMOutputString += MTMakeA('class', 'subexpanded', MTMSubExpandColor);
      MTMOutputString += MTMakeA('class', 'subclosed', MTMSubClosedColor) + '</style>';
    }

    MTMOutputString += '</head><body ';
    if(MTMBackground != "") {
      MTMOutputString += 'background="' + MTMPreHREF + MTMenuImageDirectory + MTMBackground + '" ';
    }
    MTMOutputString += 'bgcolor="' + MTMBGColor + '" text="' + MTMTextColor + '" link="' + MTMLinkColor + '" vlink="' + MTMLinkColor + '" alink="' + MTMLinkColor + '">';
    MTMOutputString += '<table border="0" cellpadding="0" cellspacing="0" width="' + MTMTableWidth + '">';
    MTMOutputString += '<tr valign="top"><td nowrap><img src="' + MTMPreHREF + MTMenuImageDirectory + MTMRootIcon + '" align="left" border="0" vspace="0" hspace="0">';
    if(MTMUseStyle) {
      MTMOutputString += '<span id="root">&nbsp;' + MTMenuText + '</span>';
    } else {
      MTMOutputString += '<font size="' + MTMRootFontSize + '" face="' + MTMRootFont + '" color="' + MTMRootColor + '">' + MTMenuText + '</font>';
    }
    MTMDoc.writeln(MTMOutputString + '</td></tr>');

    MTMListItems(menu);

    MTMDoc.writeln('</table></body></html>');
    MTMDoc.close();

    if((MTMClickedItem || MTMTrackedItem) && (MTMNN4 || MTMIE4) && !MTMFirstRun) {
      MTMItemName = "sub" + (MTMClickedItem ? MTMClickedItem : MTMTrackedItem);
      if(document.layers && parent.frames[MTMenuFrame].scrollbars) {    
        MTMyval = parent.frames[MTMenuFrame].document.anchors[MTMItemName].y;
        MTMWinSize = parent.frames[MTMenuFrame].innerHeight;
      } else {
        MTMyval = MTMGetPos(parent.frames[MTMenuFrame].document.all[MTMItemName]);
        MTMWinSize = parent.frames[MTMenuFrame].document.body.offsetHeight;
      }
      if(MTMyval > (MTMWinSize - 60)) {
        parent.frames[MTMenuFrame].scrollBy(0, parseInt(MTMyval - (MTMWinSize * 1/3)));
      }
    }

    MTMClickedItem = false;
    MTMExpansion = false;
    MTMTrack = false;
  }
MTMUpdating = false;
}

function MTMListItems(menu) {
  var i, isLast;
  for (i = 0; i < menu.items.length; i++) {
    MTMIndices[MTMLevel] = i;
    isLast = (i == menu.items.length -1);
    MTMDisplayItem(menu.items[i], isLast);

    if (menu.items[i].submenu && menu.items[i].expanded) {
      MTMBar[MTMLevel] = (isLast) ? false : true;
      MTMLevel++;
      MTMListItems(menu.items[i].submenu);
      MTMLevel--;
    } else {
      MTMBar[MTMLevel] = false;
    } 
  }
}

function MTMDisplayItem(item, last) {
  var i, img, more;

  if(item.submenu) {
    var MTMouseOverText;

    var MTMClickCmd;
    var MTMDblClickCmd = false;
    var MTMfrm = "parent.frames['code']";
    var MTMref = '.menu.items[' + MTMIndices[0] + ']';

    if(MTMLevel > 0) {
      for(i = 1; i <= MTMLevel; i++) {
        MTMref += ".submenu.items[" + MTMIndices[i] + "]";
      }
    }

    if(!MTMEmulateWE && !item.expanded && (item.url != "")) {
      MTMClickCmd = "return " + MTMfrm + ".MTMSubAction(" + MTMfrm + MTMref + ",true);";
    } else {
      MTMClickCmd = "return " + MTMfrm + ".MTMSubAction(" + MTMfrm + MTMref + ",false);";
    }

    if(item.url == "") {
      MTMouseOverText = (item.text.indexOf("'") != -1) ? MTMEscapeQuotes(item.text) : item.text;
    } else {
      MTMouseOverText = "Expand/Collapse";
    }
  }

  MTMOutputString = '<tr valign="top"><td nowrap>';
  if(MTMLevel > 0) {
    for (i = 0; i < MTMLevel; i++) {
      MTMOutputString += (MTMBar[i]) ? MTMakeImage("menu_bar.gif") : MTMakeImage("menu_pixel.gif");
    }
  }

  more = false;
  if(item.submenu) {
    if(MTMSubsGetPlus || MTMEmulateWE) {
      more = true;
    } else {
      for (i = 0; i < item.submenu.items.length; i++) {
        if (item.submenu.items[i].submenu) {
          more = true;
        }
      }
    }
  }
  if(!more) {
    img = (last) ? "menu_corner.gif" : "menu_tee.gif";
  } else {
    if(item.expanded) {
      img = (last) ? "menu_corner_minus.gif" : "menu_tee_minus.gif";
    } else {
      img = (last) ? "menu_corner_plus.gif" : "menu_tee_plus.gif";
    }
    if(item.url == "" || item.expanded || MTMEmulateWE) {
      MTMOutputString += MTMakeVoid(item, MTMClickCmd, MTMouseOverText);
    } else {
      MTMOutputString += MTMakeLink(item, true)  + ' onclick="' + MTMClickCmd + '">';
    }
  }
  MTMOutputString += MTMakeImage(img);

  if(item.submenu) {
    if(MTMEmulateWE && item.url != "") {
      MTMOutputString += '</a>' + MTMakeLink(item, false) + '>';
    }

    img = (item.expanded) ? "menu_folder_open.gif" : "menu_folder_closed.gif";

    if(!more) {
      if(item.url == "" || item.expanded) {
        MTMOutputString += MTMakeVoid(item, MTMClickCmd, MTMouseOverText);
      } else {
        MTMOutputString += MTMakeLink(item, true) + ' onclick="' + MTMClickCmd + '">';
      }
    }
    MTMOutputString += MTMakeImage(img);

  } else {
    MTMOutputString += MTMakeLink(item, true) + '>';
    img = (item.icon != "") ? item.icon : MTMFetchIcon(item.url);
    MTMOutputString += MTMakeImage(img);
  }

  if(item.submenu && (item.url != "") && (item.expanded && !MTMEmulateWE)) {
    MTMOutputString += '</a>' + MTMakeLink(item, false) + '>';
  }

  if(MTMNN3 && !MTMLinkedSS) {
    var stringColor;
    if(item.submenu && (item.url == "") && (item.number == MTMClickedItem)) {
      stringColor = (item.expanded) ? MTMSubExpandColor : MTMSubClosedColor;
    } else if(MTMTrackedItem && MTMTrackedItem == item.number) {
      stringColor = MTMTrackColor;
    } else {
      stringColor = MTMLinkColor;
    }
    MTMOutputString += '<font color="' + stringColor + '" size="' + MTMenuFontSize + '" face="' + MTMenuFont + '">';
  }
  MTMOutputString += '&nbsp;' + item.text + ((MTMNN3 && !MTMLinkedSS) ? '</font>' : '') + '</a>' ;
  MTMDoc.writeln(MTMOutputString + '</td></tr>');
}

function MTMEscapeQuotes(myString) {
  var newString = "";
  var cur_pos = myString.indexOf("'");
  var prev_pos = 0;
  while (cur_pos != -1) {
    if(cur_pos == 0) {
      newString += "\\";
    } else if(myString.charAt(cur_pos-1) != "\\") {
      newString += myString.substring(prev_pos, cur_pos) + "\\";
    } else if(myString.charAt(cur_pos-1) == "\\") {
      newString += myString.substring(prev_pos, cur_pos);
    }
    prev_pos = cur_pos++;
    cur_pos = myString.indexOf("'", cur_pos);
  }
  return(newString + myString.substring(prev_pos, myString.length));
}

function MTMTrackExpand(thisMenu) {
  var i, targetPath;
  var foundNumber = false;
  for(i = 0; i < thisMenu.items.length; i++) {
    if(thisMenu.items[i].url != "" && MTMTrackTarget(thisMenu.items[i].target)) {
      targetPath = parent.frames[thisMenu.items[i].target].location.pathname;
      if(targetPath.lastIndexOf(thisMenu.items[i].url) != -1 && (targetPath.lastIndexOf(thisMenu.items[i].url) + thisMenu.items[i].url.length) == targetPath.length && !foundNumber) {
        return(thisMenu.items[i].number);
      }
    }
    if(thisMenu.items[i].submenu && !foundNumber) {
      if(thisMenu.items[i].expanded) {
        foundNumber = MTMTrackExpand(thisMenu.items[i].submenu);
        if(foundNumber) {
          return(foundNumber);
        }
      } else {
        thisMenu.items[i].expanded = true;
        foundNumber = MTMTrackExpand(thisMenu.items[i].submenu);
        if(foundNumber) {
          MTMClickedItem = thisMenu.items[i].number;
          MTMExpansion = true;
          return(foundNumber);
        } else {
          thisMenu.items[i].expanded = false;
        }
      }
    }
  }
return(foundNumber);
}

function MTMCloseSubs(thisMenu) {
  var i, j;
  var foundMatch = false;
  for(i = 0; i < thisMenu.items.length; i++) {
    if(thisMenu.items[i].submenu && thisMenu.items[i].expanded) {
      if(thisMenu.items[i].number == MTMClickedItem) {
        foundMatch = true;
        for(j = 0; j < thisMenu.items[i].submenu.items.length; j++) {
          if(thisMenu.items[i].submenu.items[j].submenu && thisMenu.items[i].submenu.items[j].expanded) {
            thisMenu.items[i].submenu.items[j].expanded = false;
          }
        }
      } else {
        if(foundMatch) {
          thisMenu.items[i].expanded = false; 
        } else {
          foundMatch = MTMCloseSubs(thisMenu.items[i].submenu);
          if(!foundMatch) {
            thisMenu.items[i].expanded = false;
          }
        }
      }
    }
  }
return(foundMatch);
}

function MTMFetchIcon(testString) {
  var i;
  for(i = 0; i < MTMIconList.items.length; i++) {
    if((MTMIconList.items[i].type == 'any') && (testString.indexOf(MTMIconList.items[i].match) != -1)) {
      return(MTMIconList.items[i].file);
    } else if((MTMIconList.items[i].type == 'pre') && (testString.indexOf(MTMIconList.items[i].match) == 0)) {
      return(MTMIconList.items[i].file);
    } else if((MTMIconList.items[i].type == 'post') && (testString.indexOf(MTMIconList.items[i].match) != -1)) {
      if((testString.lastIndexOf(MTMIconList.items[i].match) + MTMIconList.items[i].match.length) == testString.length) {
        return(MTMIconList.items[i].file);
      }
    }
  }
return("menu_link_default.gif");
}

function MTMGetPos(myObj) {
  return(myObj.offsetTop + ((myObj.offsetParent) ? MTMGetPos(myObj.offsetParent) : 0));
}

function MTMCheckURL(myURL) {
  var tempString = "";
  if((myURL.indexOf("http://") == 0) || (myURL.indexOf("https://") == 0) || (myURL.indexOf("mailto:") == 0) || (myURL.indexOf("ftp://") == 0) || (myURL.indexOf("telnet:") == 0) || (myURL.indexOf("news:") == 0) || (myURL.indexOf("gopher:") == 0) || (myURL.indexOf("nntp:") == 0) || (myURL.indexOf("javascript:") == 0)) {
    tempString += myURL;
  } else {
    tempString += MTMPreHREF + myURL;
  }
return(tempString);
}

function MTMakeVoid(thisItem, thisCmd, thisText) {
  var tempString = "";
  tempString +=  '<a name="sub' + thisItem.number + '" href="javascript:parent.frames[\'code\'].myVoid();" onclick="' + thisCmd + '" onmouseover="window.status=\'' + thisText + '\';return true;" onmouseout="window.status=\'' + window.defaultStatus + '\';return true;"';
  if(thisItem.number == MTMClickedItem) {
    var tempClass;
    tempClass = thisItem.expanded ? "subexpanded" : "subclosed";
    tempString += ' class="' + tempClass + '"';
  }
  return(tempString + '>');
}

function MTMakeLink(thisItem, addName) {
  var tempString = '<a';

  if(MTMTrackedItem && MTMTrackedItem == thisItem.number) {
    tempString += ' class="tracked"'
  }
  if(addName) {
    tempString += ' name="sub' + thisItem.number + '"';
  }
  tempString += ' href="' + MTMCheckURL(thisItem.url) + '"';
  if(thisItem.target != "") {
    tempString += 'target="' + thisItem.target + '"';
  }
return tempString;
}

function MTMakeImage(thisImage) {
  return('<img src="' + MTMPreHREF + MTMenuImageDirectory + thisImage + '" align="left" border="0" vspace="0" hspace="0" width="18" height="18">');
}

function MTMakeBackImage(thisImage) {
  var tempString = 'transparent url("' + ((MTMPreHREF == "") ? "" : MTMPreHREF);
  tempString += MTMenuImageDirectory + thisImage + '")'
  return(tempString);
}

function MTMakeA(thisType, thisText, thisColor) {
  var tempString = "";
  tempString += 'a' + ((thisType == "pseudo") ? ':' : '.');
  return(tempString + thisText + '{color:' + thisColor + ';background:' + MTMakeBackground() + ';}');
}

function MTMakeBackground() {
  return((MTMBackground == "") ? MTMBGColor : 'transparent');
}

function MTMTrackTarget(thisTarget) {
  if(thisTarget.charAt(0) == "_") {
    return false;
  } else {
    for(i = 0; i < MTMFrameNames.length; i++) {
      if(thisTarget == MTMFrameNames[i]) {
        return true;
      }
    }
  }
  return false;
}

//  End -->
