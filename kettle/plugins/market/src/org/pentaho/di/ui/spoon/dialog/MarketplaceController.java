/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.ui.spoon.dialog;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class MarketplaceController extends AbstractXulEventHandler {

  public void openMarketPlace() throws KettleException {
    showMarketPlaceDialog();
  }

  public static void showMarketPlaceDialog() {
    MarketplaceDialog marketplaceDialog = new MarketplaceDialog( Spoon.getInstance().getShell() );
    marketplaceDialog.open();
  }

  public String getName() {
    return "MarketplaceController";
  }
}
