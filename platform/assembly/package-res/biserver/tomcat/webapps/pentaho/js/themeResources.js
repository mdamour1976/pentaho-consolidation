/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

var docHead = document.getElementsByTagName("head")[0];

(function() {

if(window.location.href.indexOf("theme=") > -1){
  var startIdx = window.location.href.indexOf("theme=")+("theme=".length);
  var endIdx = window.location.href.indexOf("&", startIdx) > -1 ? window.location.href.indexOf("&", startIdx) : window.location.href.length;
  active_theme = window.location.href.substring(startIdx, endIdx);
}

var originalOnLoad = window.onload;
window.onload = function () {
  if (originalOnLoad) {
    originalOnLoad();
  }
  customizeThemeStyling();
}

if(window.core_theme_tree){
  includeResources(core_theme_tree);
}

if(window.module_theme_tree){
  includeResources(module_theme_tree);
}

function addStylesheet(url) {
    var link = document.createElement('link');
    link.rel = 'stylesheet';
    link.type = 'text/css';
    link.href = url + (document.all ? ("?ts=" + (new Date().getTime())) : "");
    document.getElementsByTagName('head')[0].appendChild(link);
}

function includeResources(resourceTree) {
  var activeTheme = resourceTree && resourceTree[active_theme];
  if(!activeTheme) { return; }
  
  var cssPat = /\.css$/;
  var resources = activeTheme.resources;
  for(var i = 0; i < resources.length; i++){
    var baseName = resources[i];
    var basePath = CONTEXT_PATH + activeTheme.rootDir;
    if(cssPat.test(baseName)){
      addStylesheet(basePath + baseName);
      
      // Check to see if we're in a mobile device, if so add a "-mobile"
      if(navigator.userAgent.match(/(iPad|iPod|iPhone)/) != null){
        addStylesheet(basePath + baseName.replace('.css', '') + '-mobile.css');
      }
    } else {
      document.write("<script type='text/javascript' src='" + basePath + baseName + "'></script>");
    }
  }
}

}());

function customizeThemeStyling() {
  // if it is IE, inject an IE class to the body tag to allow for custom IE css by --> .IE .myclass {}
  if (document.all) {

    var className = " IE";

    // Add specific version
    var regEx = new RegExp("MSIE ([0-9]{1,})[\.0-9]{0,}");
    if (regEx.exec(navigator.userAgent) != null) {
      var version = parseInt( RegExp.$1 );
      className += " IE" + version;
    }
    
    document.getElementsByTagName("body")[0].className += className;
  }
}
