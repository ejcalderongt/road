package com.dts.roadp;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebHistoryItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacturaRes extends PBase {

	private ListView listView;
	private TextView lblPago,lblFact,lblTalon,lblMPago,lblCred;
	private ImageView imgBon,imgMPago,imgCred;
	
	private List<String> spname = new ArrayList<String>();
	private ArrayList<clsClasses.clsCDB> items= new ArrayList<clsClasses.clsCDB>();
	private ListAdaptTotals adapter;
	
	private Runnable printcallback,printclose;
	
	private clsDescGlob clsDesc;
	private printer prn;
	private clsDocFactura fdoc;
	
	private int fecha,fechae,fcorel,clidia,media;
	private String itemid,cliid,corel,sefect,fserie,desc1,svuelt;
	private int cyear, cmonth, cday, dweek,stp=0;
	
	private double dmax,dfinmon,descpmon,descg,descgmon,descgtotal,tot,stot0,stot,descmon,totimp,totperc,credito;
	private boolean acum,cleandprod,peexit,pago,saved,rutapos;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factura_res);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
		lblPago = (TextView) findViewById(R.id.TextView01);
		lblFact = (TextView) findViewById(R.id.lblFact);
		lblTalon = (TextView) findViewById(R.id.lblTalon);
		lblMPago = (TextView) findViewById(R.id.lblCVence);
		lblCred = (TextView) findViewById(R.id.TextView02);
		
		imgBon = (ImageView) findViewById(R.id.imageView6);
		imgMPago = (ImageView) findViewById(R.id.imageView1);
		imgCred = (ImageView) findViewById(R.id.imageView3);
		
		cliid=gl.cliente;	
		rutapos=gl.rutapos;
		media=gl.media;
		credito=gl.credito;
		gl.cobroPendiente = false;
			
		if (rutapos) {
			lblMPago.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblCred.setText("Pago\nTarjeta");	
			//imgCred.setImageResource(R.drawable.card_credit);
		} else {
			lblMPago.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblCred.setText("Pago\nCredito");	
			//imgCred.setImageResource(R.drawable.credit);
		}	
		
		if (media==1) {
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblMPago.setVisibility(View.INVISIBLE);
		}

		if (media <= 3){
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		}

		if (media==4) {
			imgCred.setVisibility(View.VISIBLE);
			lblCred.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
			
			if (credito<=0) {
				imgCred.setVisibility(View.INVISIBLE);
				lblCred.setVisibility(View.INVISIBLE);	
			}			
		}
		
		fechae=fecha;
		dweek=mu.dayofweek();
		
		clsDesc=new clsDescGlob(this);
		
		descpmon=totalDescProd();
		
		dmax=clsDesc.dmax;
		acum=clsDesc.acum;

		try {
			db.execSQL("DELETE FROM T_PAGO");
		} catch (SQLException e) {
		}
		
		processFinalPromo();
		
		printcallback= new Runnable() {
		    public void run() {
		    	askPrint();
		    }
		};
		
		printclose= new Runnable() {
		    public void run() {
		    	FacturaRes.super.finish();
		    }
		};
		
		prn=new printer(this,printclose);
		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp);
		saved=false;
		assignCorel();
		
		cliPorDia();
	}
	
	
	// Events
	
	public void prevScreen(View view) {
		clearGlobals();
		super.finish();
	}
	
	public void paySelect(View view) {
		
		if (fcorel==0) {
			msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
		}
				
		gl.pagoval=tot;
		gl.pagolim=tot;
		gl.pagocobro=false;
		browse=1;
		
		Intent intent = new Intent(this,Pago.class);
		startActivity(intent);	
	}
	
	public void payCash(View view) {
		
		if (fcorel==0) {
			msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
		}
		
		//inputEfectivo();  
		inputVuelto();
	}
	
	public void payCred(View view) {
		if (fcorel==0) {
			msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
		}

		inputCredito();	
	}
	
	public void showBon(View view) {
		Intent intent = new Intent(this,BonVenta.class);
		startActivity(intent);	
	}
	
	
	// Main
	
	private void processFinalPromo(){
		
		descg=gl.descglob;
		descgtotal=gl.descgtotal;

		//descgmon=(double) (stot0*descg/100);
		descgmon=(double) (descg*descgtotal/100);
		totalOrder();

		if (descg>0) {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					showPromo();
				}
			}, 300);
		}
	}
	
	public void showPromo(){
		try {
			browse=1;
			gl.promprod="";
			gl.promcant=0;
			gl.promdesc=descg;
			
			Intent intent = new Intent(this,DescBon.class);
			startActivity(intent);	
		} catch (Exception e) {
			mu.msgbox( e.getMessage());
		}
		
	}
	
	private void updDesc(){
		descg=gl.promdesc;
		//descgmon=(double) (stot0*descg/100);
		descgmon=(double) (descg*descgtotal/100);
		totalOrder();	
	}
	
	private void totalOrder(){
		double dmaxmon;
		
		cleandprod=false;
		
		if (acum) {
			dfinmon=descpmon+descgmon;
			cleandprod=false;	
		} else {
			if (descpmon>=descgmon) {
				dfinmon=descpmon;
				cleandprod=false;
			} else {	
				dfinmon=descgmon;
				cleandprod=true;
			}
		}
		
		dmaxmon=(double) (stot0*dmax/100);
		if (dmax>0) {
			if (dfinmon>dmaxmon) dfinmon=dmax;	
		}
		
		descmon=mu.round2(dfinmon);
		stot=mu.round2(stot0);
		
		fillTotals();
		
	}
	
	private void fillTotals() {
		clsClasses.clsCDB item;	
				
		items.clear();
		
		try {
			
			if (gl.sinimp) {
				
				totimp=mu.round2(totimp);
				stot=stot-totimp;
				
				totperc=stot*(gl.percepcion/100);
				totperc=mu.round2(totperc);
				
				tot=stot+totimp-descmon+totperc;
				tot=mu.round2(tot);
								
				
				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="Impuesto";item.Desc=mu.frmcur(totimp);item.Bandera=0;
				items.add(item);				
		
				if (gl.contrib.equalsIgnoreCase("C")) {
					item = clsCls.new clsCDB();
					item.Cod="Percepcion";item.Desc=mu.frmcur(totperc);item.Bandera=0;
					items.add(item);
				}
				
				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);					
				
			} else {
								
				totimp=mu.round2(totimp);
				tot=stot-descmon;
				tot=mu.round2(tot);
				
				
				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);			
				
			}
					
		} catch (Exception e) {
		}
		
		adapter=new ListAdaptTotals(this,items);
		listView.setAdapter(adapter);
	}
	
 	private void finishOrder(){

 		if (!saved) {
 			if (!saveOrder()) return;
 		}
 		
 		clsBonifSave bonsave=new clsBonifSave(this,corel,"V");
 		
 		bonsave.ruta=gl.ruta;
 		bonsave.cliente=gl.cliente;
 		bonsave.fecha=fecha;
 		bonsave.emp=gl.emp;
 		
		bonsave.save();
		
		if (prn.isEnabled()) {
					
			if (gl.peModal.equalsIgnoreCase("APR")) {
				fdoc.buildPrintExt(corel,2,"APR");
			} else if (gl.peModal.equalsIgnoreCase("...")) {	
				//
			} else {	
				fdoc.buildPrint(corel,0);
			}

			prn.printask(printclose);

			/*
			final Handler shandler = new Handler();
			shandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					//Intent intent = new Intent(FacturaRes.this,PrintDialog.class);
					//startActivity(intent);				
				}
			}, 500);
			*/	
			
		}
		
		gl.closeCliDet=true;
		gl.closeVenta=true;

		if (!prn.isEnabled()){
			super.finish();
		}

		
		/*
		if (prn.isEnabled()) {


			fdoc.buildPrint(corel);


			singlePrint();
		} else {
			gl.closeCliDet=true;
			gl.closeVenta=true;
			
			super.finish();			
		}
		*/		
		
	}
 	
 	private void singlePrint() {
 		prn.printask(printcallback);
 	}

 	//#HS_20181212 Funcion para proceso pendiente de pago.
	public void pendientePago(View view){
		askPendientePago();
	}

	private boolean saveOrder(){
		Cursor DT;
		double peso;
		int mitem;		
		
		corel=gl.ruta+"_"+mu.getCorelBase();
		fecha=du.getActDateTime();
		
		try {
			sql="SELECT MAX(ITEM) FROM D_FACT_LOG";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			mitem=DT.getInt(0);
		} catch (Exception e) {
			mitem=0;
		}
		mitem++;
		
		try {
			
			sql="SELECT SUM(TOTAL),SUM(DESMON),SUM(IMP),SUM(PESO) FROM T_VENTA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			//desc=DT.getDouble(1);
			//tot=DT.getDouble(0)-desc;
			//imp=DT.getDouble(2);
			peso=DT.getDouble(3);
			
			db.beginTransaction();
			    			
			ins.init("D_FACTURA");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",fecha);
			ins.add("EMPRESA",gl.emp);
			ins.add("RUTA",gl.ruta);
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			
			ins.add("KILOMETRAJE",0);
			ins.add("FECHAENTR",fecha);
			ins.add("FACTLINK"," ");
	   		ins.add("TOTAL",tot);
			ins.add("DESMONTO",descmon);
			ins.add("IMPMONTO",totimp+totperc);
			ins.add("PESO",peso);
			
			ins.add("BANDERA","N");
			ins.add("STATCOM","N");
			ins.add("CALCOBJ","N");
			ins.add("SERIE",fserie);
			ins.add("CORELATIVO",fcorel);
			ins.add("IMPRES",1);
			
			ins.add("ADD1",gl.ref1);
			ins.add("ADD2",gl.ref2);
			ins.add("ADD3",gl.ref3);
			
			ins.add("DEPOS","");
			ins.add("PEDCOREL","");
			ins.add("REFERENCIA","");
			ins.add("ASIGNACION","");    
			ins.add("SUPERVISOR","");
			ins.add("AYUDANTE",gl.ayudanteID);//#HS_20181207 Agregue parametro de ayudanteID
			ins.add("VEHICULO",gl.vehiculoID);//#HS_20181207 Agregue parametro de vehiculoID
			ins.add("CODIGOLIQUIDACION",0);
			ins.add("RAZON_ANULACION","");
    		
			db.execSQL(ins.sql());
						
			sql="SELECT PRODUCTO,CANT,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2,UM,FACTOR,UMSTOCK FROM T_VENTA";
			DT=Con.OpenDT(sql);
	
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
			  	ins.init("D_FACTURAD");
				ins.add("COREL",corel);
				ins.add("PRODUCTO",DT.getString(0));
				ins.add("EMPRESA",gl.emp);
				ins.add("ANULADO","N");
				ins.add("CANT",DT.getDouble(1));
				ins.add("PRECIO",DT.getDouble(2));
				ins.add("IMP",DT.getDouble(3));
				ins.add("DES",DT.getDouble(4));
				ins.add("DESMON",DT.getDouble(5));
				ins.add("TOTAL",DT.getDouble(6));
				ins.add("PRECIODOC",DT.getDouble(7));
				ins.add("PESO",DT.getDouble(8));
				ins.add("VAL1",DT.getDouble(9));
				ins.add("VAL2",DT.getString(10));				
				ins.add("UMVENTA",DT.getString(11));
				ins.add("FACTOR",DT.getDouble(12));
				ins.add("UMSTOCK",DT.getString(13));
				ins.add("UMPESO",gl.umpeso); //#HS_20181120_1625 Se agrego el valor gl.umpeso anteriormente estaba ""
				
			    db.execSQL(ins.sql());

				//#HS_20181120_1625 Se agrego parametro porque cambio la funcion
			    if (esProductoConStock(DT.getString(0))) rebajaStockUM(DT.getString(0),DT.getString(13),DT.getDouble(1),DT.getDouble(12),DT.getString(11));

			    DT.moveToNext();
			}

			// Pago

			if(!gl.cobroPendiente) {

				sql = "SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				DT = Con.OpenDT(sql);

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					ins.init("D_FACTURAP");
					ins.add("COREL", corel);
					ins.add("ITEM", DT.getInt(0));
					ins.add("ANULADO", "N");
					ins.add("EMPRESA", gl.emp);
					ins.add("CODPAGO", DT.getInt(1));
					ins.add("TIPO", DT.getString(2));
					ins.add("VALOR", DT.getDouble(3));
					ins.add("DESC1", DT.getString(4));
					ins.add("DESC2", DT.getString(5));
					ins.add("DESC3", DT.getString(6));
					ins.add("DEPOS", "");

					db.execSQL(ins.sql());

					DT.moveToNext();
				}

			}else{

				try {

						ins.init("P_COBRO");

						ins.add("DOCUMENTO", corel);
						ins.add("EMPRESA", gl.emp);
						ins.add("RUTA", gl.ruta);
						ins.add("CLIENTE", gl.cliente);
						ins.add("TIPODOC", "R");
						ins.add("VALORORIG", tot);
						ins.add("SALDO", tot);
						ins.add("CANCELADO", 0);
						ins.add("FECHAEMIT", fecha);
						ins.add("FECHAV", fecha);
						ins.add("CONTRASENA", corel);
						ins.add("ID_TRANSACCION", 0);
						ins.add("REFERENCIA", 0);
						ins.add("ASIGNACION", 0);

						db.execSQL(ins.sql());

					Toast.makeText(this, "Se guardo la factura pendiente de pago",Toast.LENGTH_LONG).show();

				}catch (Exception e){
					mu.msgbox("PendientePago: "+e.getMessage());
				}

			}


			
			// Datos facturacion

			ins.init("D_FACTURAF");

			ins.add("COREL",corel);
			ins.add("NOMBRE",gl.fnombre);
			ins.add("NIT",gl.fnit);
			ins.add("DIRECCION",gl.fdir);
			
			db.execSQL(ins.sql());
					
			
			// Actualizacion de ultimo correlativo
			
			sql="UPDATE P_COREL SET CORELULT="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";	
			db.execSQL(sql);
			
			ins.init("D_FACT_LOG");
			ins.add("ITEM",mitem);
			ins.add("SERIE",fserie);
			ins.add("COREL",fcorel);
			ins.add("FECHA",0);
			ins.add("RUTA",gl.ruta);
			db.execSQL(ins.sql());
					
			db.setTransactionSuccessful();
				
			db.endTransaction();
			 
			saved=true;
			
		} catch (Exception e) {
			db.endTransaction();
		   	mu.msgbox("Error (factura) " + e.getMessage());return false;
		}
		
		try {
			upd.init("P_CLIRUTA");
			upd.add("BANDERA",0);
			upd.Where("CLIENTE='"+cliid+"' AND DIA="+dweek);
	
			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			mu.msgbox("Error  : " + e.getMessage());
		}	
		
		saveAtten(tot);
		
		return true;
	}
	
	private void rebajaStockUM(String prid,String umstock,double cant,double factor, String umventa) {
		Cursor DT;
		double acant,val,disp,cantapl;
		String lote,doc,stat;

		acant=cant*factor;

		sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV FROM P_STOCK WHERE (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"')";
		DT=Con.OpenDT(sql);
		DT.moveToFirst();
	
		val=DT.getDouble(0);
		lote=DT.getString(4);
		doc=DT.getString(5);
		stat=DT.getString(9);

		cantapl=acant;
		disp=val-acant;
		acant=acant-val;


		// Stock

		sql="UPDATE P_STOCK SET CANT="+disp+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"') AND (UNIDADMEDIDA='"+umstock+"')";
		db.execSQL(sql);

		// Factura Stock

		ins.init("D_FACTURA_STOCK");

		ins.add("COREL",corel);
		ins.add("CODIGO",prid );
		ins.add("CANT",cantapl );
		ins.add("CANTM",DT.getDouble(1));
		ins.add("PESO",DT.getDouble(2));
		ins.add("plibra",DT.getDouble(3));
		ins.add("LOTE",lote );

		ins.add("DOCUMENTO",doc);
		ins.add("FECHA",DT.getInt(6));
		ins.add("ANULADO",DT.getInt(7));
		ins.add("CENTRO",DT.getString(8));
		ins.add("STATUS",stat);
		ins.add("ENVIADO",DT.getInt(10));
		ins.add("CODIGOLIQUIDACION",DT.getInt(11));
		ins.add("COREL_D_MOV",DT.getString(12));
		ins.add("UNIDADMEDIDA",umstock);

		db.execSQL(ins.sql());

		// Factura lotes

		try {
			ins.init("D_FACTURAD_LOTES");

			ins.add("COREL",corel);
			ins.add("PRODUCTO",prid );
			ins.add("LOTE",lote );
			ins.add("CANTIDAD",cantapl);
			ins.add("PESO",0);
			ins.add("UMSTOCK",umstock);
			ins.add("UMPESO",gl.umpeso);
			ins.add("UMVENTA",umventa);

			db.execSQL(ins.sql());

			//Toast.makeText(this,ins.SQL(),Toast.LENGTH_LONG).show();

		} catch (SQLException e) {
			mu.msgbox(e.getMessage()+"\n"+ins.sql());
		}

		if (acant<=0) return;

	}
	
	private void rebajaStock(String prid,double cant) {
		Cursor DT;
		double acant,val,disp,cantapl;
		String lote,doc,stat;
		
		acant=cant;
			
		sql="SELECT CANT,LOTE,DOCUMENTO,STATUS FROM P_STOCK WHERE CODIGO='"+prid+"'";
		DT=Con.OpenDT(sql);

		DT.moveToFirst();
		while (!DT.isAfterLast()) {
				
			val=DT.getDouble(0);
			lote=DT.getString(1);
			doc=DT.getString(2);
			stat=DT.getString(3);
			
			if (val>acant) {
				cantapl=acant;
				disp=val-acant;
			} else {
				cantapl=val;
				disp=0;
			}
			acant=acant-val;
			
			// Stock
			
			sql="UPDATE P_STOCK SET CANT="+disp+" WHERE CODIGO='"+prid+"' AND LOTE='"+lote+"' AND DOCUMENTO='"+doc+"' AND STATUS='"+stat+"'";
			db.execSQL(sql);
			
			// Factura lotes
				
			try {
				ins.init("D_FACTURAD_LOTES");
				
				ins.add("COREL",corel);
				ins.add("PRODUCTO",prid );
				ins.add("LOTE",lote );
				ins.add("CANTIDAD",cantapl);
				ins.add("PESO",0);
				
				db.execSQL(ins.sql());
				
				//Toast.makeText(this,ins.SQL(),Toast.LENGTH_LONG).show();
				
			} catch (SQLException e) {
				mu.msgbox(e.getMessage()+"\n"+ins.sql());
			}
			
			if (acant<=0) return;
				
		    DT.moveToNext();
		}

	}
		
	private void saveAtten(double tot) {
		int ti,tf,td;
		
		ti=gl.atentini;tf=du.getActDateTime();
		td=du.timeDiff(tf,ti);if (td<1) td=1;
		
		try {
			ins.init("D_ATENCION");
		
			ins.add("RUTA",gl.ruta);
			ins.add("FECHA",ti);
			ins.add("HORALLEG",gl.ateninistr);
			//ins.add("HORALLEG",DU.shora(ti)+":00");
			ins.add("HORASAL",du.shora(tf)+":00");
			ins.add("TIEMPO",td);
			
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("DIAACT",du.dayofweek(ti));
			ins.add("DIA",du.dayofweek(ti));
			ins.add("DIAFLAG","S");
			
			ins.add("SECUENCIA",1);
			ins.add("SECUENACT",1);
			ins.add("CODATEN","");
			ins.add("KILOMET",0);
			
			ins.add("VALORVENTA",tot);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",clidia);
			ins.add("CODOPER","V");
			ins.add("COREL",corel);
			
			ins.add("SCANNED",gl.escaneo);
			ins.add("STATCOM","N");
			ins.add("LLEGO_COMPETENCIA_ANTES",0);
			
			ins.add("CoorX",gl.gpspx);
			ins.add("CoorY",gl.gpspy);
			ins.add("CliCoorX",gl.gpscpx);
			ins.add("CliCoorY",gl.gpscpy);
			ins.add("Dist",gl.gpscdist);
			
			db.execSQL(ins.sql());
			
		} catch (SQLException e) {			
			//String s=gl.ruta+" / "+ti+" / "+gl.ateninistr;
			mu.msgbox("Error (att) : " + e.getMessage());
		}	
		
	}
	
	private double totalDescProd(){
		Cursor DT;
		
		try {
			sql="SELECT SUM(DESMON),SUM(TOTAL),SUM(IMP) FROM T_VENTA";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			tot=DT.getDouble(1);
			stot0=tot+DT.getDouble(0);
			
			totimp=DT.getDouble(2);
			
			return DT.getDouble(0);
		} catch (Exception e) {
			tot=0;
			mu.msgbox( e.getMessage());return 0;
		}	
		
	}
 	
	private void assignCorel(){
		Cursor DT;
		int ca,ci,cf,ca1,ca2;
		
		fcorel=0;fserie="";
			
		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN FROM P_COREL WHERE RUTA='"+gl.ruta+"'";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			fserie=DT.getString(0);
			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
			
		} catch (Exception e) {
			fcorel=0;fserie="";
			mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n"+e.getMessage());
			return;
		}	
			
		try {
			sql="SELECT MAX(COREL) FROM D_FACT_LOG WHERE RUTA='"+gl.ruta+"' AND SERIE='"+fserie+"'";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			ca2=DT.getInt(0);
		} catch (Exception e) {
			ca2=0;
		}
		
		ca=ca1;if (ca2>ca) ca=ca2;
		fcorel=ca+1;
		
		if (fcorel>cf) {
			mu.msgbox("Se ha acabado el talonario de facturas. No se puede continuar con la venta.");
			fcorel=0;return;
		}

		//#HS_20181128_1602 Cambie el texto del mensaje.
		if (fcorel==cf) mu.msgbox("Esta es la última factura disponible.");
		
		lblFact.setText("Factura : "+fserie+" - "+fcorel);
		
		s="Talonario : "+fcorel+" / "+cf+"\n";
		s=s+"Disponible : "+(cf-fcorel);
		lblTalon.setText(s);
		
	}
	
	private boolean esProductoConStock(String prcodd) {
		Cursor DT;
		
		try {
			sql="SELECT TIPO FROM P_PRODUCTO WHERE CODIGO='"+prcodd+"'";
           	DT=Con.OpenDT(sql);
           	DT.moveToFirst();
			
           	return DT.getString(0).equalsIgnoreCase("P");
		} catch (Exception e) {
			return false;
	    }		
	}
	
	
	// Pago
	
	private void inputEfectivo() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Pago Efectivo");
		alert.setMessage("Monto a pagar");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText(""+tot);
		input.requestFocus();
		
		showkeyb();
			
		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=false;
		    	sefect=input.getText().toString();
		    	closekeyb();
		    	applyCash();
		    	checkPago();
		  	}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=true;
				closekeyb();
			}
		});

		alert.show();
	}
	
	private void inputVuelto() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("A pagar : "+mu.frmcur(tot));
		alert.setMessage("Pagado con billete : ");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText("");
		input.requestFocus();
		
		showkeyb();
			
		alert.setPositiveButton("Vuelto", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				double pg,vuel;
					
				peexit=false;
				svuelt=input.getText().toString();
				sefect=""+tot;
				
				try {
					pg=Double.parseDouble(svuelt);
					if (pg<tot) {
						msgbox("Monto menor que total");return;
					}
					
					vuel=pg-tot;
				} catch (NumberFormatException e) {
					msgbox("Monto incorrecto");return;
				}
						
		    	applyCash();
		    	if (vuel==0) {
		    		checkPago();
		    	} else {	
		    		vuelto("Vuelto : "+mu.frmcur(vuel));
		    		//dialog.dismiss();
		    	}    	
		    	
		  	}
		});

		alert.setNegativeButton("Exacto", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=false;
				svuelt=""+tot;
		    	sefect=""+tot;
		    	applyCash();
		    	checkPago();
			}
		});
		
		alert.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=true;
			}
		});

		alert.show();
	}
	
	public void vuelto(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
		
		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {	
    	    	checkPago();
    	    }
    	});
		dialog.show();
	
	} 
	
	private void applyCash() {
		double epago;
		
		try {
			epago=Double.parseDouble(sefect);
			if (epago==0) return;
			
			if (epago<0) throw new Exception();
			
			//if (epago>plim) {
			//	MU.msgbox("Total de pago mayor que total de saldos.");return;
			//}
			
			//if (epago>tsel) {
			//	msgAskOverPayd("Total de pago mayor que saldo\nContinuar");return;
			//}
			
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
			
			ins.init("T_PAGO");
				
			ins.add("ITEM",1);
			ins.add("CODPAGO",1);
			ins.add("TIPO","E");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");
				
		    db.execSQL(ins.sql());
			
			//msgAskSave("Aplicar pago y crear un recibo");
			
		} catch (Exception e) {
			inputEfectivo(); 
			mu.msgbox("Pago incorrecto"+e.getMessage());	   	
	    }
		
	}
	
	private void inputCredito() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Pago Credito");
		alert.setMessage("Valor a pagar");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText(""+tot);
		input.requestFocus();
		
		showkeyb();
		
		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=false;
		    	sefect=input.getText().toString();
		    	closekeyb();
		    	applyCredit();
		    	checkPago();
		  	}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				peexit=true;
				closekeyb();
			}
		});

		alert.show();
	}
	
	private void applyCredit() {
		double epago;
		
		try {
			epago=Double.parseDouble(sefect);
			if (epago==0) return;
			
			if (epago<0) throw new Exception();
			
			//if (epago>plim) {
			//	MU.msgbox("Total de pago mayor que total de saldos.");return;
			//}
			
			//if (epago>tsel) {
			//	msgAskOverPayd("Total de pago mayor que saldo\nContinuar");return;
			//}
			
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
			
			ins.init("T_PAGO");
				
			ins.add("ITEM",1);
			ins.add("CODPAGO",4);
			ins.add("TIPO","K");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");
				
		    db.execSQL(ins.sql());
			
			//msgAskSave("Aplicar pago y crear un recibo");
			
		} catch (Exception e) {
			inputEfectivo(); 
			mu.msgbox("Pago incorrecto"+e.getMessage());	   	
	    }
		
	}
	
	private void inputCard() {
	
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Numero de tarjeta");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER);	
		input.setText("");input.requestFocus();
		
		showkeyb();
		
		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
		    	if (checkNum(input.getText().toString())) addPagoTar();	
		  	}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				closekeyb();
			}
		});

		alert.show();
	}
	
	private boolean checkNum(String s) {
		
		if (mu.emptystr(s)) {
			showkeyb();
			inputCard(); 
			mu.msgbox("Numero incorrecto");showkeyb();
			return false;
		}
		
		desc1=s;
		return true;
		
	}
	
	private void addPagoTar(){
		
		sql="DELETE FROM T_PAGO";
		db.execSQL(sql);
		
		try {
			
			ins.init("T_PAGO");
			
			ins.add("ITEM",1);
			ins.add("CODPAGO",3);
			ins.add("TIPO","K");
			ins.add("VALOR",tot);
			ins.add("DESC1",desc1);
			ins.add("DESC2","");
			ins.add("DESC3","");
			
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		checkPago();
		
	}
	
	private void checkPago() {
		Cursor DT;
		double tpago;
		
		try {
			sql="SELECT SUM(VALOR) FROM T_PAGO";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			tpago=DT.getDouble(0);
		} catch (Exception e) {
			tpago=0;
			mu.msgbox( e.getMessage());
		}
		
		s=mu.frmcur(tpago);
		
		if (tpago<tot) {
			lblPago.setText("Pago incompleto.\n"+s);
			pago=false;	
		} else {
			lblPago.setText("Pago COMPLETO.\n"+s);
			pago=true;
			//if (rutapos) askSavePos(); else askSave();			
			finishOrder();
		}
		
	}

	
	// Aux
	
	public void askSave(View view) {
		checkPago();
	}
	
	private void askSave() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Road");
		dialog.setMessage("Guardar la factura ?");
					
		dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	finishOrder();
		    }
		});
		
		dialog.setNegativeButton("Salir", null);
		
		dialog.show();
			
	}
	
	private void askSavePos() {
		double vuel;
		String sv="";
		
		try {
			vuel=Double.parseDouble(svuelt);
			
			if (vuel<tot) throw new Exception();
						
			if (vuel>tot) {
				vuel=vuel-tot;
				sv="Vuelto : "+mu.frmcur(vuel)+"\n\n";
			} else {	
				sv="SIN VUELTO";
			}
			
		} catch (Exception e) {
			sv="";
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Road");
		dialog.setMessage(sv+"Guardar la factura ?");
					
		dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	finishOrder();
		    }
		});
		
		dialog.setNegativeButton("Salir", null);
		
		dialog.show();
			
	}

	//#HS_20181212 Dialogo para Pendiente de pago
	private void askPendientePago() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Road");
		dialog.setMessage("¿Esta seguro de cobrar despues?");

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				gl.cobroPendiente = true;
				finishOrder();
			}
		});

		dialog.setNegativeButton("Cancelar", null);

		dialog.show();

	}

	private void askPrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Road");
		dialog.setMessage("Impresión correcta ?");
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {	
		    	
				gl.closeCliDet=true;
				gl.closeVenta=true;
				FacturaRes.super.finish();		
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	singlePrint();
		    }
		});
		
		
		dialog.show();
			
	}	
	
	private void clearGlobals() {
		try {
			db.execSQL("DELETE FROM T_PAGO");
		} catch (SQLException e) {
		}	
		try {
			db.execSQL("DELETE FROM T_BONITEM WHERE PRODID='*'");
		} catch (SQLException e) {
		}	
	}
	
	private void checkPromo() {
		Cursor DT;
		
		imgBon.setVisibility(View.INVISIBLE);
		
		try {
			sql="SELECT ITEM FROM T_BONITEM";
           	DT=Con.OpenDT(sql);
			if (DT.getCount()>0) imgBon.setVisibility(View.VISIBLE);
		} catch (Exception e) {
	    }			
	}
	
	private void cliPorDia() {
		Cursor DT;
		
		int dweek=mu.dayofweek();
		
		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);
			clidia=DT.getCount();
		} catch (Exception e) {
			clidia=0;
		}
			
	}
	
	
	// Aux
	
	private void hidekeyboard() {
		View sview = this.getCurrentFocus();
		
		if (sview != null) {  
		    InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(sview.getWindowToken(), 0);
		}
	}
	
	
	// Activity Events
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    checkPromo();
	    
	    checkPago();
	    if (browse==1) {
	    	browse=0;
	    	if (gl.promapl) updDesc();
	    	return;
	    }
	
	}	

	@Override
	public void onBackPressed() {
		clearGlobals();
		super.onBackPressed();
	}
	
	
}
