/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Oct 25, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.solutionbrowser.tree;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author wseyler
 *
 */
public class LeafItemWidget extends Composite {
  Image leafImage;
  Label leafLabel;
  
  public LeafItemWidget(String title, String imageUrl) {
    HorizontalPanel widget = new HorizontalPanel();
    initWidget(widget);
    
    leafImage = new Image(imageUrl);
    widget.add(leafImage);
    
    leafLabel = new Label(title);
    leafLabel.removeStyleName("gwt-Label");
    leafLabel.addStyleName("gwt-TreeItem");
    widget.add(leafLabel);
  }
  public Image getLeafImage() {
    return leafImage;
  }

  public Label getLeafLabel() {
    return leafLabel;
  }
}
