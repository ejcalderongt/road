package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import datamaxoneil.connection.Connection_Bluetooth;

// 00:17:AC:15:EC:C3

public class printDMax extends printBase {
	
	private String ss,ess;
	private appGlobals appG;
	private boolean validprint;
	
	public printDMax(Context context,String printerMAC,boolean validprinter) {
		super(context,printerMAC);
		validprint=validprinter;
		appG = new appGlobals();
	}
	
	
	// Main
	
	public void printask(Runnable callBackHook) {
			
		hasCallback=true;
		callback=callBackHook;
		
		fname="print.txt";errmsg="";exitprint=false;
		msgAskPrint();
	}
	
	public void printask() {
		hasCallback=false;
		
		fname="print.txt";errmsg="";exitprint=false;
		msgAskPrint();				
	}

	public void printask(Runnable callBackHook, String fileName){
		hasCallback=true;
		callback=callBackHook;

		fname=fileName;errmsg="";exitprint=false;
		msgAskPrint();
	}

	public void printnoask(Runnable callBackHook, String fileName){
		hasCallback=true;
		callback=callBackHook;

		fname=fileName;errmsg="";
		try {
			if (loadFile())  doStartPrint();
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
		}
	}
	
	public boolean print() 	{
		hasCallback=false;
		
		fname="print.txt";errmsg="";

		try	{
			if (loadFile())	doStartPrint();	else return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	
	public void printask(String fileName) {
		hasCallback=false;
		
		fname=fileName;	errmsg="";
		exitprint=false;
		msgAskPrint();				
	}
		
	public boolean print(String fileName) {
		hasCallback=false;
		
		fname=fileName;errmsg="";
		
		try 		{
			if (loadFile())	{
				doStartPrint();
			} else {
				return false;
			}
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	

	// Private
	
	private boolean loadFile() {

		File ffile;
		BufferedReader dfile;
		String ss;
		
		try {
			
			File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
			ffile = new File(file1.getPath());
					
			FileInputStream fIn = new FileInputStream(ffile);
			dfile = new BufferedReader(new InputStreamReader(fIn));
			
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
			return false;
		}			
		
		try {
			docLP.clear();
			
			while ((ss = dfile.readLine()) != null) {
					docLP.writeText(ss);
			}
			
			docLP.writeText("");
			docLP.writeText("");
			
			dfile.close();	
				       
	        printData = docLP.getDocumentData();
	
			return true;

		} catch (Exception e) {
			try {
				dfile.close();
			} catch (Exception e1) {}	
			
			showmsg("Error: " + e.getMessage());
			
			return false;
		}			
	}
	
	private void doStartPrint() {
		if (!validprint) {
			showmsg("¡La impresora no está autorizada!");return;
		}

		appG.endPrint = true;
		showmsg("Imprimiendo ..." );
		AsyncPrintCall wsRtask = new AsyncPrintCall();
		wsRtask.execute();
	}
	
	private class AsyncPrintCall extends AsyncTask<String, Void, Void> {

		@Override
	    protected Void doInBackground(String... params) {
			try {
				processPrint();
			} catch (Exception e) {
				Log.d("Err_Impr",e.getMessage());
			}
	            
	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
	    	try {
	    		doCallBack();
			} catch (Exception e) {}
	    }
	 
        @Override
        protected void onPreExecute() {}
	 
        @Override
        protected void onProgressUpdate(Void... values) {}
	 
    }	
	
	private void doCallBack() {

		if (!hasCallback) return;

        try {
            final Handler cbhandler = new Handler();
            cbhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.run();
                    } catch (Exception ee) {}
                }
            }, 500);
        } catch (Exception e) {
        	ess=e.getMessage();
		}

	}
	
	public void processPrint() {
		
		ss="p1..";
		
		try {

			prconn = null;

			//Looper.prepare();

			prconn = Connection_Bluetooth.createClient(printerAddress);

			if (!prconn.getIsOpen()) prconn.open();

			prconn.write(printData);

			prthread.sleep(1500);

			prconn.clearWriteBuffer();
			//printclose.run();
			prconn.close();

		} catch (Exception e) {
			ss = ss + "Error : " + e.getMessage();
			Log.d("processPrint_ERR: ", ss);

			try {
				if (prconn != null) prconn.close();
			} catch (Exception ee) {
			}
		}	
		
	}

	private void msgAskRePrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿Quiere volver a intentar la impresión?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				msgAskPrint();
			}

		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
                            appG.devprncierre=true;
  							printclose.run();
						} catch (Exception e) {
							//showmsg(e.getMessage());
						}

					}
				}, 200);
			}
		});

		dialog.show();

	}

	// Aux
	
	private void msgAskPrint() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿La impresora está lista?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	try {
					if (loadFile())
						doStartPrint();
				} catch (Exception e) {
					showmsg("Error: " + e.getMessage());
				}
		    }

		});

		//#EJC20181130:Se comentarió por solicitud de auditor de SAT.
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						msgAskRePrint();
					}
				}, 200);
		    }
		});

		dialog.show();
			
	}
	
	public void showmsg(String MsgStr) {
		errmsg=MsgStr;
			handler.post(new Runnable() {
			public void run() {
				Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	

}
