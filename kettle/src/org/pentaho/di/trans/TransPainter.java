/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.AreaOwner.AreaType;
import org.pentaho.di.core.gui.BasePainter;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.GCInterface.EColor;
import org.pentaho.di.core.gui.GCInterface.EFont;
import org.pentaho.di.core.gui.GCInterface.EImage;
import org.pentaho.di.core.gui.GCInterface.ELineStyle;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Rectangle;
import org.pentaho.di.core.gui.ScrollBarInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;


public class TransPainter extends BasePainter
{
	
	private static Class<?> PKG = TransPainter.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    public static final String STRING_PARTITIONING_CURRENT_STEP = "PartitioningCurrentStep"; // $NON-NLS-1$
	public static final String STRING_REMOTE_INPUT_STEPS        = "RemoteInputSteps";        // $NON-NLS-1$
	public static final String STRING_REMOTE_OUTPUT_STEPS       = "RemoteOutputSteps";       // $NON-NLS-1$
	public static final String STRING_STEP_ERROR_LOG            = "StepErrorLog";            // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_COPY             = "HopTypeCopy";             // $NON-NLS-1$
	
	public static final String[] magnificationDescriptions = 
		new String[] { "  200% ", "  150% ", "  100% ", "  75% ", "  50% ", "  25% "};
	
	
    private TransMeta    transMeta;

    private TransHopMeta candidate;
    
	private Map<StepMeta, String> stepLogMap;
	private List<StepMeta>	mouseOverSteps;
	private StepMeta    startHopStep;
	private Point       endHopLocation;
	private StepMeta    endHopStep;
	private StepMeta       noInputStep;
	private StreamType	candidateHopType;
	private boolean 	startErrorHopStep;
	private StepMeta showTargetStreamsStep;
	private Trans trans;
	private boolean slowStepIndicatorEnabled;

    public TransPainter(GCInterface gc, TransMeta transMeta, 
                        Point area, 
                        ScrollBarInterface hori, ScrollBarInterface vert, 
                        TransHopMeta candidate, Point drop_candidate, Rectangle selrect,
                        List<AreaOwner> areaOwners, 
                        List<StepMeta> mouseOverSteps,     		
                        int iconsize, int linewidth, int gridsize, int shadowSize, boolean antiAliasing, 
                		String noteFontName, int noteFontHeight, Trans trans, boolean slowStepIndicatorEnabled
                        )
    {
    	super(gc, transMeta, area, hori, vert, drop_candidate, selrect, areaOwners,
        		iconsize, linewidth, gridsize, shadowSize, antiAliasing, 
        		noteFontName, noteFontHeight
    		);
        this.transMeta      = transMeta;
        
        this.candidate      = candidate;
        
        this.mouseOverSteps  = mouseOverSteps;
        
        this.trans = trans;
        this.slowStepIndicatorEnabled = slowStepIndicatorEnabled;        
        
        stepLogMap = null;
    }
    
  public TransPainter(GCInterface gc, TransMeta transMeta, Point area, ScrollBarInterface hori, ScrollBarInterface vert, 
      TransHopMeta candidate, Point drop_candidate, Rectangle selrect, List<AreaOwner> areaOwners, List<StepMeta> mouseOverSteps,
      int iconsize, int linewidth, int gridsize, int shadowSize, boolean antiAliasing, String noteFontName, int noteFontHeight) {
    
    this(gc, transMeta, area, hori, vert, candidate, drop_candidate, selrect, areaOwners, mouseOverSteps, iconsize, linewidth, gridsize, shadowSize, 
        antiAliasing, noteFontName, noteFontHeight, new Trans(transMeta), false);
  }
    
    private static String[] getPeekTitles(){
		String[] titles = {
				
			BaseMessages.getString(PKG, "PeekMetric.Column.Copynr"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Read"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Written"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Input"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Output"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Updated"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Rejected"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Errors"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Active"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Time"),
			BaseMessages.getString(PKG, "PeekMetric.Column.Speed"),
			BaseMessages.getString(PKG, "PeekMetric.Column.PriorityBufferSizes")						
		};
		return titles;
    }    

    public void buildTransformationImage()
    {        
        Point max   = transMeta.getMaximum();
        Point thumb = getThumb(area, max);
        offset = getOffset(thumb, area);

        // First clear the image in the background color
        gc.setBackground(EColor.BACKGROUND);
        gc.fillRectangle(0, 0, area.x, area.y);
        
        // If there is a shadow, we draw the transformation first with an alpha setting
        //
        if (shadowSize>0) {
        	shadow = true;
        	gc.setTransform(translationX, translationY, shadowSize, magnification);
            gc.setAlpha(20);
        	
        	drawTrans(thumb);
        }
        
        // Draw the transformation onto the image
        //
        shadow = false;
    	gc.setTransform(translationX, translationY, 0, magnification);
        gc.setAlpha(255);
        drawTrans(thumb);
        
        gc.dispose();
    }

    private void drawTrans(Point thumb)
    {
        if (!shadow && gridSize>1) {
        	drawGrid();
        }
        
        if (hori!=null && vert!=null)
        {
            hori.setThumb(thumb.x);
            vert.setThumb(thumb.y);
        }
        
        gc.setFont(EFont.NOTE);
        
        // First the notes
        for (int i = 0; i < transMeta.nrNotes(); i++)
        {
            NotePadMeta ni = transMeta.getNote(i);
            drawNote(ni);
        }

        gc.setFont(EFont.GRAPH);
        gc.setBackground(EColor.BACKGROUND);

        for (int i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            drawHop(hi);
        }

        if (candidate != null)
        {
            drawHop(candidate, true);
        } else {
	        if (startHopStep!=null && endHopLocation!=null) {
	        	Point fr = startHopStep.getLocation();
	        	Point to = endHopLocation;
	        	if (endHopStep==null) {
	        		gc.setForeground(EColor.GRAY);
	        	} else {
	        		gc.setForeground(EColor.BLUE);
	        	}
	        	Point start = real2screen(fr.x+iconsize/2, fr.y+iconsize/2);
	        	Point end = real2screen(to.x, to.y);
	        	drawArrow(start.x, start.y, end.x, end.y, theta, calcArrowLength(), 1.2, startHopStep, endHopStep==null ? endHopLocation : endHopStep);
	        }  else if (endHopStep!=null && endHopLocation!=null) {
	        	Point fr = endHopLocation;
	        	Point to = endHopStep.getLocation();
	        	if (startHopStep==null) {
	        		gc.setForeground(EColor.GRAY);
	        	} else {
	        		gc.setForeground(EColor.BLUE);
	        	}
	        	Point start = real2screen(fr.x, fr.y);
	        	Point end = real2screen(to.x+iconsize/2, to.y+iconsize/2);
	        	drawArrow(start.x, start.y, end.x, end.y, theta, calcArrowLength(), 1.2, startHopStep==null ? endHopLocation : startHopStep, endHopStep);
	        }

        }

        // Draw regular step appearance
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStep(stepMeta);
        }
        
        if (slowStepIndicatorEnabled){

            // Highlight possible bottlenecks
            for (int i = 0; i < transMeta.nrSteps(); i++)
            {
                StepMeta stepMeta = transMeta.getStep(i);
                if (stepMeta.isDrawn()) checkDrawSlowStepIndicator(stepMeta);
            }
        	
        }
        
        // Draw step status indicators (running vs. done)
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStepStatusIndicator(stepMeta);
        }

        // Draw performance table for selected step(s)
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStepPerformanceTable(stepMeta);
        }
        
        // Display an icon on the indicated location signaling to the user that the step in question does not accept input 
        //
        if (noInputStep!=null) {
        	gc.setLineWidth(2);	
        	gc.setForeground(EColor.RED);
        	Point n = noInputStep.getLocation();
        	gc.drawLine(n.x-5, n.y-5, n.x+iconsize+10, n.y+iconsize+10);
        	gc.drawLine(n.x-5, n.y+iconsize+5, n.x+iconsize+5, n.y-5);
        }

        if (drop_candidate != null)
        {
            gc.setLineStyle(ELineStyle.SOLID);
            gc.setForeground(EColor.BLACK);
            Point screen = real2screen(drop_candidate.x, drop_candidate.y);
            gc.drawRectangle(screen.x, screen.y,          iconsize, iconsize);
        }
        
        if (!shadow) {
        	drawRect(selrect);
        }

    }

	private void checkDrawSlowStepIndicator(StepMeta stepMeta) {
		
		if (stepMeta == null){
			return;
		}
		
		// draw optional performance indicator
		if (trans != null) {
			
			Point pt = stepMeta.getLocation();
			if (pt == null) {
				pt = new Point(50, 50);
			}

			Point screen = real2screen(pt.x, pt.y);
			int x = screen.x;
			int y = screen.y;
			
			List<StepInterface> steps = trans.findBaseSteps(stepMeta.getName());
			for (StepInterface step : steps) {
				if (step.isRunning()) {

					int inputRows = step.rowsetInputSize();
					int outputRows = step.rowsetOutputSize();
						
					// if the step can't keep up with its input, mark it by drawing an animation
					boolean isSlow = inputRows * 0.85 > outputRows;
					if (isSlow) {
						gc.setLineWidth(linewidth + 1);
						if (System.currentTimeMillis() % 2000 > 1000){
							gc.setForeground(EColor.BACKGROUND);
							gc.setLineStyle(ELineStyle.SOLID);
							gc.drawRectangle(x + 1, y + 1, iconsize -2, iconsize - 2);
							
							gc.setForeground(EColor.DARKGRAY);
							gc.setLineStyle(ELineStyle.DOT);
							gc.drawRectangle(x + 1, y + 1, iconsize -2, iconsize - 2);
						}
						else{
							gc.setForeground(EColor.DARKGRAY);
							gc.setLineStyle(ELineStyle.SOLID);
							gc.drawRectangle(x + 1, y + 1, iconsize -2, iconsize - 2);
							
							gc.setForeground(EColor.BACKGROUND);
							gc.setLineStyle(ELineStyle.DOT);
							gc.drawRectangle(x + 1, y + 1, iconsize -2, iconsize - 2);
						}
						
						
					}
					
				}
				gc.setLineStyle(ELineStyle.SOLID);
			}
		}
	}
    
	private void drawStepPerformanceTable(StepMeta stepMeta) {
		
		if (stepMeta == null){
			return;
		}
		
		// draw optional performance indicator
		if (trans != null) {
			
			Point pt = stepMeta.getLocation();
			if (pt == null) {
				pt = new Point(50, 50);
			}

			Point screen = real2screen(pt.x, pt.y);
			int x = screen.x;
			int y = screen.y;
			
			List<StepInterface> steps = trans.findBaseSteps(stepMeta.getName());

			// draw mouse over performance indicator
			if (trans.isRunning()) {

				if (stepMeta.isSelected()) {
									
					// determine popup dimensions up front
					int popupX = x;
					int popupY = y;
					
					int popupWidth = 0;
					int popupHeight = 1;

					gc.setFont(EFont.SMALL);
					Point p = gc.textExtent("0000000000");
					int colWidth = p.x+MINI_ICON_MARGIN;
					int rowHeight = p.y+MINI_ICON_MARGIN;
					int titleWidth = 0;

					// calculate max title width to get the colum with 
					String[] titles = TransPainter.getPeekTitles();
					
					for(String title : titles){
						Point titleExtent = gc.textExtent(title);
						titleWidth = Math.max(titleExtent.x+MINI_ICON_MARGIN, titleWidth);
						popupHeight += titleExtent.y + MINI_ICON_MARGIN;
					}
					
					popupWidth = titleWidth + 2*MINI_ICON_MARGIN;
					
					// determine total popup width
					popupWidth += steps.size() * colWidth;
					
					// determine popup position
					popupX = popupX + (iconsize - popupWidth)/2;
					popupY = popupY - popupHeight - MINI_ICON_MARGIN;
					
					// draw the frame
					gc.setForeground(EColor.DARKGRAY);
					gc.setBackground(EColor.LIGHTGRAY);
					gc.setLineWidth(1);
					gc.fillRoundRectangle(popupX, popupY, popupWidth, popupHeight, 7, 7);
					// draw the title columns
//					gc.setBackground(EColor.BACKGROUND);
//					gc.fillRoundRectangle(popupX, popupY, titleWidth+MINI_ICON_MARGIN, popupHeight, 7, 7);
					gc.setBackground(EColor.LIGHTGRAY);
					gc.drawRoundRectangle(popupX, popupY, popupWidth, popupHeight, 7, 7);
					
					for (int i=0, barY=popupY;i<titles.length;i++){
						// fill each line with a slightly different background color
						
						if(i%2 == 1){
							gc.setBackground(EColor.BACKGROUND);
						}
						else{
							gc.setBackground(EColor.LIGHTGRAY);	
						}
						gc.fillRoundRectangle(popupX+1, barY+1, popupWidth-2, rowHeight, 7, 7);
						barY += rowHeight;
						
					}
					
					// draw the header column
					int rowY = popupY+MINI_ICON_MARGIN;
					int rowX = popupX+MINI_ICON_MARGIN;
					
					gc.setForeground(EColor.BLACK);
					gc.setBackground(EColor.BACKGROUND);

					for (int i=0;i<titles.length;i++){
						if(i%2 == 1){
							gc.setBackground(EColor.BACKGROUND);
						}
						else{
							gc.setBackground(EColor.LIGHTGRAY);	
						}						
						gc.drawText(titles[i], rowX, rowY);
						rowY += rowHeight;
					}

					// draw the values for each copy of the step
					gc.setBackground(EColor.LIGHTGRAY);
					rowX += titleWidth;

					for(StepInterface step: steps){

						rowX += colWidth;
						rowY = popupY+MINI_ICON_MARGIN;

						StepStatus stepStatus = new StepStatus(step);
						String[] fields = stepStatus.getPeekFields();

						for(int i=0;i<fields.length;i++){
							if(i%2 == 1){
								gc.setBackground(EColor.BACKGROUND);
							}
							else{
								gc.setBackground(EColor.LIGHTGRAY);	
							}							
							drawTextRightAligned(fields[i], rowX, rowY);
							rowY += rowHeight;
						}
						
					}

				}
			}

		}
	}
	
	private void drawStepStatusIndicator(StepMeta stepMeta) {
		
		if (stepMeta == null){
			return;
		}
		
		// draw status indicator
		if (trans != null) {
			
			Point pt = stepMeta.getLocation();
			if (pt == null) {
				pt = new Point(50, 50);
			}

			Point screen = real2screen(pt.x, pt.y);
			int x = screen.x;
			int y = screen.y;
			
			List<StepInterface> steps = trans.findBaseSteps(stepMeta.getName());

			for(StepInterface step: steps){
				if (step.getStatus().equals(StepExecutionStatus.STATUS_FINISHED)){
					gc.drawImage(EImage.TRUE, x+iconsize-7, y-7);
				}
			}

		}
	}
	

	private void drawTextRightAligned(String txt, int x, int y){
		int off = gc.textExtent(txt).x;
		x -= off;
		gc.drawText(txt, x, y);
	}
	
	
	private void drawHop(TransHopMeta hi)
    {
        drawHop(hi, false);
    }
	

    private void drawHop(TransHopMeta hi, boolean isCandidate)
    {
        StepMeta fs = hi.getFromStep();
        StepMeta ts = hi.getToStep();

        if (fs != null && ts != null)
        {
            drawLine(fs, ts, hi, isCandidate);
        }
    }

    private void drawStep(StepMeta stepMeta)
    {
        if (stepMeta == null) return;
        int alpha = gc.getAlpha();
        
        StepIOMetaInterface ioMeta = stepMeta.getStepMetaInterface().getStepIOMeta();


        Point pt = stepMeta.getLocation();
        if (pt==null) {
        	pt = new Point(50,50);
        }

        Point screen = real2screen(pt.x, pt.y);
        int x = screen.x;
        int y = screen.y;
        
        boolean stepError = false;
        if (stepLogMap!=null && !stepLogMap.isEmpty()) {
        	String log = stepLogMap.get(stepMeta);
	        	if (!Const.isEmpty(log)) {
	        		stepError=true;
	        	}
        	}

        // REMOTE STEPS
        
        // First draw an extra indicator for remote input steps...
        //
        if (!stepMeta.getRemoteInputSteps().isEmpty()) {
        	gc.setLineWidth(1);
        	gc.setForeground(EColor.GRAY);
        	gc.setBackground(EColor.BACKGROUND);
            gc.setFont(EFont.GRAPH);
        	String nrInput = Integer.toString(stepMeta.getRemoteInputSteps().size());
        	Point textExtent = gc.textExtent(nrInput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the left
        	//
        	Point point = new Point(x-iconsize-iconsize/2, y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrInput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// 
        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, x-iconsize/2, point.y+textExtent.y/2);
         	drawArrow(x-iconsize/2, point.y+textExtent.y/2, x+iconsize/3, y, Math.toRadians(15), 15, 1.8, null, null );
         	
            // Add to the list of areas...
            if (!shadow) {
            	areaOwners.add(new AreaOwner(AreaType.REMOTE_INPUT_STEP, point.x, point.y, textExtent.x, textExtent.y, offset, stepMeta, STRING_REMOTE_INPUT_STEPS));
            }
        }

        // Then draw an extra indicator for remote output steps...
        //
        if (!stepMeta.getRemoteOutputSteps().isEmpty()) {
        	gc.setLineWidth(1);
        	gc.setForeground(EColor.GRAY);
        	gc.setBackground(EColor.BACKGROUND);
            gc.setFont(EFont.GRAPH);
        	String nrOutput = Integer.toString(stepMeta.getRemoteOutputSteps().size());
        	Point textExtent = gc.textExtent(nrOutput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the right
        	//
        	Point point = new Point(x+2*iconsize+iconsize/2-textExtent.x, y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrOutput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// This time, we start at the left side...
        	// 
        	gc.drawLine(point.x, point.y+textExtent.y/2, x+iconsize+iconsize/2, point.y+textExtent.y/2);
         	drawArrow(x+2*iconsize/3, y, x+iconsize+iconsize/2, point.y+textExtent.y/2, Math.toRadians(15), 15, 1.8, null, null );

         	// Add to the list of areas...
            if (!shadow) {
            	areaOwners.add(new AreaOwner(AreaType.REMOTE_OUTPUT_STEP, point.x, point.y, textExtent.x, textExtent.y, offset, stepMeta, STRING_REMOTE_OUTPUT_STEPS));
            }
        }
        
        // PARTITIONING

        // If this step is partitioned, we're drawing a small symbol indicating this...
        //
        if (stepMeta.isPartitioned()) {
        	gc.setLineWidth(1);
        	gc.setForeground(EColor.RED);
        	gc.setBackground(EColor.BACKGROUND);
            gc.setFont(EFont.GRAPH);
            
            PartitionSchema partitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
            if (partitionSchema!=null) {
	            
            	String nrInput;
            	
            	if (partitionSchema.isDynamicallyDefined()) {
            		nrInput = "Dx"+partitionSchema.getNumberOfPartitionsPerSlave();
            	}
            	else {
            		nrInput = "Px"+Integer.toString(partitionSchema.getPartitionIDs().size());
            	}
	        	
	        	Point textExtent = gc.textExtent(nrInput);
	        	textExtent.x+=2; // add a tiny little bit of a margin
	        	textExtent.y+=2;
	        	
	        	// Draw it a 2 icons above the step icon.
	        	// Draw it an icon and a half to the left
	        	//
	        	Point point = new Point(x-iconsize-iconsize/2, y-iconsize-iconsize);
	        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
	        	gc.drawText(nrInput, point.x+1, point.y+1);
	        	
	        	// Now we draw an arrow from the cube to the step...
	        	// 
	        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, x-iconsize/2, point.y+textExtent.y/2);
	         	gc.drawLine(x-iconsize/2, point.y+textExtent.y/2, x+iconsize/3, y);
	         	
	         	// Also draw the name of the partition schema below the box
	         	//
	         	gc.setForeground(EColor.GRAY);
	         	gc.drawText(Const.NVL(partitionSchema.getName(), "<no partition name>"), point.x, point.y+textExtent.y+3, true);
	         	
	            // Add to the list of areas...
	         	//
	            if (!shadow) {
	            	areaOwners.add(new AreaOwner(AreaType.STEP_PARTITIONING, point.x, point.y, textExtent.x, textExtent.y, offset, stepMeta, STRING_PARTITIONING_CURRENT_STEP));
	            }
            }
        }
                        
        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        
        // Add to the list of areas...
        if (!shadow) {
        	areaOwners.add(new AreaOwner(AreaType.STEP_ICON, x, y, iconsize, iconsize, offset, transMeta, stepMeta));
        }
        
        gc.drawStepIcon(x, y, stepMeta);
        gc.setBackground(EColor.BACKGROUND);
        if (stepError) {
        	gc.setForeground(EColor.RED);
        } else {
        	gc.setForeground(EColor.BLACK);
        }
        gc.drawRectangle(x - 1, y - 1, iconsize + 1, iconsize + 1);

        Point namePosition = getNamePosition(name, screen, iconsize );
        
        gc.setForeground(EColor.BLACK);
        gc.setFont(EFont.GRAPH);
        gc.drawText(name, namePosition.x, namePosition.y, true);

        boolean partitioned=false;
        
        StepPartitioningMeta meta = stepMeta.getStepPartitioningMeta();
        if (stepMeta.isPartitioned() && meta!=null)
        {
            partitioned=true;
        }
        if (stepMeta.getClusterSchema()!=null)
        {
            String message = "C";
            if (stepMeta.getClusterSchema().isDynamic()) {
            	message+="xN";
            } else {
            	message+="x"+stepMeta.getClusterSchema().findNrSlaves();
            }
            
            gc.setBackground(EColor.BACKGROUND);
            gc.setForeground(EColor.BLACK);
            gc.drawText(message, x + 3 + iconsize, y - 8);
        }
        
        if (stepMeta.getCopies() > 1  && !partitioned)
        {
            gc.setBackground(EColor.BACKGROUND);
            gc.setForeground(EColor.BLACK);
            String copies = "x" + stepMeta.getCopies();
            Point textExtent = gc.textExtent(copies);
            //gc.fillRectangle(x - 11, y - 11, textExtent.x+2, textExtent.y+2);
            // gc.drawRectangle(x - 11, y - 11, textExtent.x+2, textExtent.y+2);
            gc.drawText(copies, x - textExtent.x/2, y - textExtent.y + 3, false);
        }
        
        // If there was an error during the run, the map "stepLogMap" is not empty and not null.  
        //
        if (stepError) {
        	String log = stepLogMap.get(stepMeta);

        	// Show an error lines icon in the lower right corner of the step...
      		//
      		int xError = x + iconsize - 5;
      		int yError = y + iconsize - 5;
      		Point ib = gc.getImageBounds(EImage.STEP_ERROR);
      		gc.drawImage(EImage.STEP_ERROR, xError, yError);
      		if (!shadow) {
      			areaOwners.add(new AreaOwner(AreaType.STEP_ERROR_ICON, pt.x + iconsize-5, pt.y + iconsize-5, ib.x, ib.y, offset, log, STRING_STEP_ERROR_LOG));
      		}
        }
        
        // Optionally drawn the mouse-over information
        //
        if (mouseOverSteps.contains(stepMeta)) {
          StepMetaInjectionInterface injectionInterface = stepMeta.getStepMetaInterface().getStepMetaInjectionInterface();

          EImage[] miniIcons;
          if (injectionInterface!=null) {
            miniIcons = new EImage[] { EImage.INPUT, EImage.EDIT, EImage.CONTEXT_MENU, EImage.OUTPUT, EImage.INJECT, };
          } else {
            miniIcons = new EImage[] { EImage.INPUT, EImage.EDIT, EImage.CONTEXT_MENU, EImage.OUTPUT, };
          }
        	
        	int totalHeight=0;
        	int totalIconsWidth=0;
        	int totalWidth=2*MINI_ICON_MARGIN;
        	for (EImage miniIcon : miniIcons) {
        		Point bounds = gc.getImageBounds(miniIcon);
        		totalWidth+=bounds.x+MINI_ICON_MARGIN;
        		totalIconsWidth+=bounds.x+MINI_ICON_MARGIN;
        		if (bounds.y>totalHeight) totalHeight=bounds.y;
        	}
        	totalHeight+=2*MINI_ICON_MARGIN;
        	        	
        	gc.setFont(EFont.SMALL);
        	String trimmedName = stepMeta.getName().length()<30 ? stepMeta.getName() : stepMeta.getName().substring(0,30);
        	Point nameExtent = gc.textExtent(trimmedName);
        	nameExtent.y+=2*MINI_ICON_MARGIN;
        	nameExtent.x+=3*MINI_ICON_MARGIN;
        	totalHeight+=nameExtent.y;
        	if (nameExtent.x>totalWidth) totalWidth=nameExtent.x;

        	int areaX = x+iconsize/2-totalWidth/2+MINI_ICON_SKEW;
        	int areaY = y+iconsize+MINI_ICON_DISTANCE;

        	gc.setForeground(EColor.DARKGRAY);
        	gc.setBackground(EColor.LIGHTGRAY);
        	gc.setLineWidth(1);
        	gc.fillRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);
        	gc.drawRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);

        	gc.setBackground(EColor.BACKGROUND);
        	gc.fillRoundRectangle(areaX+2, areaY+2, totalWidth-MINI_ICON_MARGIN+1, nameExtent.y-MINI_ICON_MARGIN, 7, 7);
        	gc.setForeground(EColor.BLACK);
        	gc.drawText(trimmedName, areaX+(totalWidth-nameExtent.x)/2+MINI_ICON_MARGIN, areaY+MINI_ICON_MARGIN, true);
        	gc.setForeground(EColor.DARKGRAY);
        	gc.setBackground(EColor.LIGHTGRAY);

        	gc.setFont(EFont.GRAPH);
        	areaOwners.add(new AreaOwner(AreaType.MINI_ICONS_BALLOON, areaX, areaY, totalWidth, totalHeight, offset, stepMeta, ioMeta));

        	
        	gc.fillPolygon(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY+2, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY+2, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });
        	
        	gc.drawPolyline(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });

        	gc.setBackground(EColor.BACKGROUND);
        	
        	// Put on the icons...
        	//
        	int xIcon = areaX+(totalWidth-totalIconsWidth)/2+MINI_ICON_MARGIN;
        	int yIcon = areaY+5+nameExtent.y;
        	for (int i=0;i<miniIcons.length;i++) {
        		EImage miniIcon = miniIcons[i];
        		Point bounds = gc.getImageBounds(miniIcon);
        		boolean enabled=false;
        		switch(i) {
        		case 0: // INPUT
        			enabled=ioMeta.isInputAcceptor() || ioMeta.isInputDynamic();
                	areaOwners.add(new AreaOwner(AreaType.STEP_INPUT_HOP_ICON, xIcon, yIcon, bounds.x, bounds.y, offset, stepMeta, ioMeta));
        			break;
        		case 1: // EDIT
        			enabled=true;
                	areaOwners.add(new AreaOwner(AreaType.STEP_EDIT_ICON, xIcon, yIcon, bounds.x, bounds.y, offset, stepMeta, ioMeta));
        			break;
        		case 2: // STEP_MENU
        			enabled=true;
        			areaOwners.add(new AreaOwner(AreaType.STEP_MENU_ICON, xIcon, yIcon, bounds.x, bounds.y, offset, stepMeta, ioMeta));
                	break;
        		case 3: // OUTPUT
        			enabled=ioMeta.isOutputProducer() || ioMeta.isOutputDynamic();
              areaOwners.add(new AreaOwner(AreaType.STEP_OUTPUT_HOP_ICON, xIcon, yIcon, bounds.x, bounds.y, offset, stepMeta, ioMeta));
        			break;
        		case 4: // INJECT
              enabled = injectionInterface!=null;
              areaOwners.add(new AreaOwner(AreaType.STEP_INJECT_ICON, xIcon, yIcon, bounds.x, bounds.y, offset, stepMeta, injectionInterface));
              break;
        		}
        		if (enabled) {
        			gc.setAlpha(255);
        		} else {
        			gc.setAlpha(100);
        		}
        		gc.drawImage(miniIcon, xIcon, yIcon);
        		xIcon+=bounds.x+5;
        	}
        	
        	// OK, see if we need to show a slide-out for target streams...
        	//
        	if (showTargetStreamsStep!=null) {
        		ioMeta = showTargetStreamsStep.getStepMetaInterface().getStepIOMeta();
        		List<StreamInterface> targetStreams = ioMeta.getTargetStreams();
        		int targetsWidth=0;
        		int targetsHeight=0;;
        		for (int i=0;i<targetStreams.size();i++) {
        			String description = targetStreams.get(i).getDescription(); 
        			Point extent = gc.textExtent(description);
        			if (extent.x>targetsWidth) targetsWidth=extent.x;
        			targetsHeight+=extent.y+MINI_ICON_MARGIN;
        		}
        		targetsWidth+=MINI_ICON_MARGIN;
        		
            	gc.setBackground(EColor.LIGHTGRAY);
        		gc.fillRoundRectangle(areaX, areaY+totalHeight+2, targetsWidth, targetsHeight, 7, 7);
        		gc.drawRoundRectangle(areaX, areaY+totalHeight+2, targetsWidth, targetsHeight, 7, 7);

        		int targetY=areaY+totalHeight+MINI_ICON_MARGIN;
        		for (int i=0;i<targetStreams.size();i++) {
        			String description = targetStreams.get(i).getDescription(); 
        			Point extent = gc.textExtent(description);
        			gc.drawText(description, areaX+MINI_ICON_MARGIN, targetY, true);
        			if (i<targetStreams.size()-1) {
        				gc.drawLine(areaX+MINI_ICON_MARGIN/2, targetY+extent.y+3, areaX+targetsWidth-MINI_ICON_MARGIN/2, targetY+extent.y+2);
        			}
        			
                	areaOwners.add(new AreaOwner(AreaType.STEP_TARGET_HOP_ICON_OPTION, areaX, targetY, targetsWidth, extent.y+MINI_ICON_MARGIN, offset, stepMeta, targetStreams.get(i)));

        			targetY+=extent.y+MINI_ICON_MARGIN;
        		}
        		
            	gc.setBackground(EColor.BACKGROUND);
        	}
        }
        
        // Restore the previous alpha value
        //
        gc.setAlpha(alpha);
    }

    public Point getNamePosition(String string, Point screen, int iconsize)
    {
        Point textsize = gc.textExtent(string);
        
        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        return new Point(xpos, ypos);
    }

    private void drawLine(StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate) {
        int line[] = getLine(fs, ts);

        EColor col;
        ELineStyle linestyle=ELineStyle.SOLID;
        int activeLinewidth = linewidth; 
        
        if (is_candidate)
        {
            col = EColor.BLUE;
        }
        else
        {
            if (hi.isEnabled())
            {
                if (fs.isSendingErrorRowsToStep(ts))
                {
                    col = EColor.RED;
                    linestyle = ELineStyle.DOT;
                    activeLinewidth = linewidth+1;
                }
                else
                {
                	col = EColor.BLACK;
                }
            }
            else
            {
                col = EColor.GRAY;
            }
        }
        if (hi.split) activeLinewidth = linewidth+2;

        // Check to see if the source step is an info step for the target step.
        //
        StepIOMetaInterface ioMeta = ts.getStepMetaInterface().getStepIOMeta();
        List<StreamInterface> infoStreams = ioMeta.getInfoStreams();
        if (!infoStreams.isEmpty()) {
        	// Check this situation, the source step can't run in multiple copies!
        	//
        	for (StreamInterface stream : infoStreams) {
        		if (fs.getName().equalsIgnoreCase(stream.getStepname())) {
        			// This is the info step over this hop!
        			//
        			if (fs.getCopies()>1) {
        				// This is not a desirable situation, it will always end in error.
        				// As such, it's better not to give feedback on it.
        				// We do this by drawing an error icon over the hop...
        				//
        				col=EColor.RED;
        			}
        		}
        	}
        }
        
        gc.setForeground(col);
        gc.setLineStyle(linestyle);
        gc.setLineWidth(activeLinewidth);
        
        drawArrow(line, fs, ts);
        
        if (hi.split) gc.setLineWidth(linewidth);

        gc.setForeground(EColor.BLACK);
        gc.setBackground(EColor.BACKGROUND);
        gc.setLineStyle(ELineStyle.SOLID);
    }


    private int[] getLine(StepMeta fs, StepMeta ts)
    {
        Point from = fs.getLocation();
        Point to = ts.getLocation();
        
        int x1 = from.x + iconsize / 2;
        int y1 = from.y + iconsize / 2;

        int x2 = to.x + iconsize / 2;
        int y2 = to.y + iconsize / 2;

        return new int[] { x1, y1, x2, y2 };
    }

    private void drawArrow(int line[], Object startObject, Object endObject)
    {
    	Point screen_from = real2screen(line[0], line[1]);
        Point screen_to = real2screen(line[2], line[3]);
        
        drawArrow(screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, calcArrowLength(), -1, startObject, endObject);
    }

    private void drawArrow(int x1, int y1, int x2, int y2, double theta, int size, double factor, Object startObject, Object endObject)
    {
        int mx, my;
        int x3;
        int y3;
        int x4;
        int y4;
        int a, b, dist;
        double angle;

        gc.drawLine(x1, y1, x2, y2);

        // in between 2 points
        mx = x1 + (x2 - x1) / 2;
        my = y1 + (y2 - y1) / 2;

        a = Math.abs(x2 - x1);
        b = Math.abs(y2 - y1);
        dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
        if (factor<0)
        {
	        if (dist >= 2 * iconsize)
	             factor = 1.3;
	        else
	             factor = 1.2;
        }

        // in between 2 points
        mx = (int) (x1 + factor * (x2 - x1) / 2);
        my = (int) (y1 + factor * (y2 - y1) / 2);
        
        // calculate points for arrowhead
        angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;

        x3 = (int) (mx + Math.cos(angle - theta) * size);
        y3 = (int) (my + Math.sin(angle - theta) * size);

        x4 = (int) (mx + Math.cos(angle + theta) * size);
        y4 = (int) (my + Math.sin(angle + theta) * size);

        gc.switchForegroundBackgroundColors();
        gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
        gc.switchForegroundBackgroundColors();
        
        if ( startObject instanceof StepMeta && endObject instanceof StepMeta) {
        	factor = 0.8;

        	StepMeta fs = (StepMeta)startObject;
        	StepMeta ts = (StepMeta)endObject;
        	
	        // in between 2 points
	        mx = (int) (x1 + factor * (x2 - x1) / 2) - 8;
	        my = (int) (y1 + factor * (y2 - y1) / 2) - 8;
	        
	        boolean errorHop = fs.isSendingErrorRowsToStep(ts) || (startErrorHopStep && fs.equals(startHopStep));
	        boolean targetHop = Const.indexOfString(ts.getName(), fs.getStepMetaInterface().getStepIOMeta().getTargetStepnames())>=0;

	        if (targetHop) {
	        	StepIOMetaInterface ioMeta = fs.getStepMetaInterface().getStepIOMeta();
	        	StreamInterface targetStream = ioMeta.findTargetStream(ts);
	        	if (targetStream!=null) {
		        	EImage hopsIcon = BasePainter.getStreamIconImage(targetStream.getStreamIcon());
		        	Point bounds = gc.getImageBounds(hopsIcon);
		        	gc.drawImage(hopsIcon, mx, my);
		        	if (!shadow) {
		    			areaOwners.add(new AreaOwner(AreaType.STEP_TARGET_HOP_ICON, mx, my, bounds.x, bounds.y, offset, fs, targetStream));
		    		}
	        	}
	        } else  if (!fs.isDistributes() && !ts.getStepPartitioningMeta().isMethodMirror() && !errorHop) {
		        
	        	Point bounds = gc.getImageBounds(EImage.COPY_ROWS); 
	        	gc.drawImage(EImage.COPY_ROWS, mx, my);
	        	
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_COPY_ICON, mx, my, bounds.x, bounds.y, offset, fs, STRING_HOP_TYPE_COPY));
	    		}
		        mx+=16;
	        } 
	        
	        if (errorHop) {
	        	Point bounds = gc.getImageBounds(EImage.COPY_ROWS);
		        gc.drawImage(EImage.ERROR, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_ERROR_ICON, mx, my, bounds.x, bounds.y, offset, fs, ts));
	    		}
		        mx+=16;
            }
	        
	        StepIOMetaInterface ioMeta = ts.getStepMetaInterface().getStepIOMeta();
	        String[] infoStepnames = ioMeta.getInfoStepnames();
	        
	        if ( (candidateHopType==StreamType.INFO && ts.equals(endHopStep) && fs.equals(startHopStep)) || Const.indexOfString(fs.getName(), infoStepnames) >= 0) {
	        	Point bounds = gc.getImageBounds(EImage.INFO);
        		gc.drawImage(EImage.INFO, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_INFO_ICON, mx, my, bounds.x, bounds.y, offset, fs, ts));
	    		}
		        mx+=16;
	        }
	        
	        // Check to see if the source step is an info step for the target step.
	        //
	        if (!Const.isEmpty(infoStepnames)) {
	        	// Check this situation, the source step can't run in multiple copies!
	        	//
	        	for (String infoStep : infoStepnames) {
	        		if (fs.getName().equalsIgnoreCase(infoStep)) {
	        			// This is the info step over this hop!
	        			//
	        			if (fs.getCopies()>1) {
	        				// This is not a desirable situation, it will always end in error.
	        				// As such, it's better not to give feedback on it.
	        				// We do this by drawing an error icon over the hop...
	        				//
	        	        	gc.drawImage(EImage.ERROR, mx, my);
	        	        	if (!shadow) {
	        	    			areaOwners.add(new AreaOwner(AreaType.HOP_INFO_STEP_COPIES_ERROR, mx, my, MINI_ICON_SIZE, MINI_ICON_SIZE, offset, fs, ts));
	        	    		}
	        		        mx+=16;
	        				
	        			}
	        		}
	        	}
	        }


        }

    }

	/**
	 * @return the translationX
	 */
	public float getTranslationX() {
		return translationX;
	}

	/**
	 * @param translationX the translationX to set
	 */
	public void setTranslationX(float translationX) {
		this.translationX = translationX;
	}

	/**
	 * @return the translationY
	 */
	public float getTranslationY() {
		return translationY;
	}

	/**
	 * @param translationY the translationY to set
	 */
	public void setTranslationY(float translationY) {
		this.translationY = translationY;
	}

	/**
	 * @return the stepLogMap
	 */
	public Map<StepMeta, String> getStepLogMap() {
		return stepLogMap;
	}

	/**
	 * @param stepLogMap the stepLogMap to set
	 */
	public void setStepLogMap(Map<StepMeta, String> stepLogMap) {
		this.stepLogMap = stepLogMap;
	}
	
	/**
	 * @param startHopStep the startHopStep to set
	 */
	public void setStartHopStep(StepMeta startHopStep) {
		this.startHopStep = startHopStep;
	}

	/**
	 * @param endHopLocation the endHopLocation to set
	 */
	public void setEndHopLocation(Point endHopLocation) {
		this.endHopLocation = endHopLocation;
	}

	/**
	 * @param noInputStep the noInputStep to set
	 */
	public void setNoInputStep(StepMeta noInputStep) {
		this.noInputStep = noInputStep;
	}

	/**
	 * @param endHopStep the endHopStep to set
	 */
	public void setEndHopStep(StepMeta endHopStep) {
		this.endHopStep = endHopStep;
	}

	public void setCandidateHopType(StreamType candidateHopType) {
		this.candidateHopType = candidateHopType;
	}

	public void setStartErrorHopStep(boolean startErrorHopStep) {
		this.startErrorHopStep = startErrorHopStep;
	}

	/**
	 * @return the showTargetStreamsStep
	 */
	public StepMeta getShowTargetStreamsStep() {
		return showTargetStreamsStep;
	}

	/**
	 * @param showTargetStreamsStep the showTargetStreamsStep to set
	 */
	public void setShowTargetStreamsStep(StepMeta showTargetStreamsStep) {
		this.showTargetStreamsStep = showTargetStreamsStep;
	}
	

}
