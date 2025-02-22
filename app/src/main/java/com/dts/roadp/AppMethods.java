package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Currency;
import java.util.Locale;

public class AppMethods {

	private Context cont;
	private appGlobals gl;
	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	private String sql;
	private String sp;

	public AppMethods(Context context,appGlobals global,BaseDatos dbconnection, SQLiteDatabase database) {

		cont=context;
		gl=global;
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}
	
	public void reconnect(BaseDatos dbconnection, SQLiteDatabase database) 	{
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}

	//Función para saber la cantidad de registros en una tabla
	public int getDocCount(String ss,String pps) {

		Cursor DT;
		int cnt =0;
		String st;

		try {
			sql=ss;
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				cnt=DT.getCount();
				st=pps+" "+cnt;
				sp=sp+st+"\n";
			}

			return cnt;
		} catch (Exception e) {
			//mu.msgbox(sql+"\n"+e.getMessage());
			return 0;
		}
	}

	//Función para saber la cantidad de registros en una tabla específica
	public int getDocCountTipo(String tipo, boolean sinEnviar) {

		Cursor DT;
		int cnt = 0;
		String st, ss;
		String pps = "";

		try {

			switch(tipo) {
				case "Facturas":

					sql="SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_FACTURA";
					sql += (sinEnviar?" WHERE STATCOM = 'N'":"");
					break;

				case "Pedidos":

					sql="SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_PEDIDO";
					sql += (sinEnviar?" WHERE STATCOM = 'N'":"");
					break;

				case "Cobros":

					sql="SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_COBRO";
					sql += (sinEnviar?" WHERE STATCOM = 'N'":"");
					break;

				case "Devolucion":

					sql="SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_NOTACRED";
					sql += (sinEnviar?" WHERE STATCOM = 'N'":"");
					break;

				case "Inventario":

					sql=" SELECT IFNULL(SUM(A.CANT),0) AS CANT " +
						" FROM (SELECT IFNULL(COUNT(DOCUMENTO),0) AS CANT FROM P_STOCK " +
						" UNION SELECT IFNULL(COUNT(DOCUMENTO),0) AS CANT FROM P_STOCKB " +
						" UNION SELECT IFNULL(COUNT(DOCUMENTO),0) AS CAN FROM P_STOCK_PALLET) A";
					break;
			}

			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
			    DT.moveToFirst();
				cnt=DT.getInt(0);
			}

			st=pps+" "+cnt;
			sp=sp+st+"\n";

		} catch (Exception e) {

		}

		return cnt;

	}


	// Public
	
	public void parametrosExtra() {
		Cursor dt;
		String sql,val="";
		int ival;

		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);

		} catch (Exception e) {
			val="N";
		}
		if (val.equalsIgnoreCase("S"))gl.peStockItf=true; else gl.peStockItf=false;

		// gl.peModal
		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=3";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peModal=dt.getString(0).toUpperCase();

		} catch (Exception e) {
			gl.peModal="-";
		}	

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=4";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
		} catch (Exception e) {
			val="N";
		}	

		if (val.equalsIgnoreCase("S"))gl.peSolicInv=true; else gl.peSolicInv=false;


		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=5";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peAceptarCarga=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peAceptarCarga=false;
		}	

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=6";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotInv=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotInv=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=7";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotPrec=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotPrec=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=8";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotStock=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotStock=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=9";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<2)  ival=2;
			if (ival>10) ival=-1;
			gl.peDec=ival;
		} catch (Exception e) {
			gl.peDec=-1;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=10";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<0)  ival=0;
			if (ival>10) ival=10;
			gl.peDecImp=ival;
		} catch (Exception e) {
			gl.peDecImp=0;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=11";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<1) ival=0;
			gl.peDecCant=ival;
		} catch (Exception e) {
			gl.peDecCant=0;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=12";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peMon=val;
		} catch (Exception e) {
			Locale defaultLocale = Locale.getDefault();
			Currency currency = Currency.getInstance(defaultLocale);
			gl.peMon=currency.getSymbol();		
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=13";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peVehAyud=val.equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peVehAyud=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=14";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peEnvioParcial=val.equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peEnvioParcial=true;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=15";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peOrdPorNombre=val.equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peOrdPorNombre=true;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=16";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peFormatoFactura=val;
		} catch (Exception e) {
			gl.peFormatoFactura="";
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=17";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peImprFactCorrecta=val.equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peImprFactCorrecta=false;
		}

	}


    // Productos

    public boolean ventaPeso(String cod) {
        Cursor DT;
        String umm;

        try {
     		String sql = "SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE CODIGO='" + cod + "' AND NIVEL="+gl.nivel;
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			umm=DT.getString(0);

			return  umm.equalsIgnoreCase(gl.umpeso);

        } catch (Exception e) {
            toast(e.getMessage());
            return false;
        }
    }

    public boolean prodBarra(String cod) {
        Cursor DT;

        try {
            String sql = "SELECT ES_PROD_BARRA FROM P_PRODUCTO WHERE CODIGO='" + cod + "'";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            return  DT.getInt(0)==1;
        } catch (Exception e) {
            //toast(e.getMessage());
            return false;
        }
    }

	public boolean ventaRepesaje(String cod) {
		Cursor DT;
		String umm;

		try {
			String sql = "SELECT VENTA_POR_PESO FROM P_PRODUCTO WHERE CODIGO='" + cod + "'";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();
			return  DT.getInt(0)==1;
		} catch (Exception e) {
			toast(e.getMessage());
			return false;
		}
	}

	public void estandartInventario()  {
		Cursor dt,df;
		String cod,ub,us,lote,doc,stat;
		double cant,cantm,fact,fact1,fact2;

		try {

			sql="SELECT P_STOCK.CODIGO,P_STOCK.UNIDADMEDIDA, P_PRODUCTO.UNIDBAS, P_STOCK.CANT, " +
					"P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.STATUS, P_STOCK.CANTM  " +
					"FROM  P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				cod=dt.getString(0);
				us=dt.getString(1);
				ub=dt.getString(2);
				cant=dt.getDouble(3);
				lote = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(6);
				cantm=dt.getDouble(7);

				if (!ub.equalsIgnoreCase(us)) {

					sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+us+"') ";
					df=Con.OpenDT(sql);
					if (df.getCount()==0) {
						msgbox("No existe factor conversion para el producto : " + cod);
						sql = "DELETE FROM P_STOCK WHERE CODIGO='" + cod + "'";
						db.execSQL(sql);
						fact1=1;
					} else {
						df.moveToFirst();
						fact1=df.getDouble(0);
					}

					sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+ub+"') ";
					df=Con.OpenDT(sql);
					if (df.getCount()==0) {
						msgbox("No existe factor conversion para el producto : "+cod);
						sql="DELETE FROM P_STOCK WHERE CODIGO='"+cod+"'";
						db.execSQL(sql);
						fact2=1;
					} else {
						df.moveToFirst();
						fact2=df.getDouble(0);
					}

					if (fact1>=fact2) {
						fact=fact1/fact2;
					} else {
						fact=fact2/fact1;
					}

					cant = cant * fact;
					cantm = cantm * fact;

					sql="UPDATE P_STOCK SET CANT=" + cant + ",CANTM=" + cantm + ",UNIDADMEDIDA='" + ub + "'  " +
						"WHERE (CODIGO='" + cod + "') AND (UNIDADMEDIDA='" + us + "') AND (LOTE='" + lote + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
					db.execSQL(sql);

				}

				dt.moveToNext();
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	public double getPeso() {
		Cursor DT;
		double sumaPesoB=0,sumaPeso=0;

		sql = "SELECT IFNULL(SUM(S.PESO),0) AS PESOTOT " +
				" FROM P_STOCKB S, P_PRODUCTO P " +
				" WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)" +
				" AND S.BARRA NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN " +
				" (SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S'))";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaPesoB=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT IFNULL(SUM(S.PESO),0) AS PESOTOT " +
		      " FROM P_STOCK_PALLET S, P_PRODUCTO P " +
		      " WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)" +
		      " AND S.BARRAPRODUCTO NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN " +
			  " (SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S'))";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaPesoB+=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT SUM(S.PESO) AS PESOTOT, SUM(S.CANT) AS CANTUNI " +
		      " FROM P_STOCK S, P_PRODUCTO P " +
		      " WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO AND S.CANT > 0";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaPeso=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT SUM(S.PESO) AS PESOTOT " +
		      " FROM P_STOCK_PALLET S, P_PRODUCTO P " +
		      " WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO AND S.CANT > 0";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaPeso+=DT.getDouble(0);
		}

		DT.close();

		sumaPeso = sumaPeso + sumaPesoB;

		return sumaPeso;
	}

	public double getCantidad() {
		Cursor DT;
		double sumaCantB=0,sumaCant=0;

		sql = "SELECT IFNULL(COUNT(S.CODIGO),0) AS CANTUNI " +
				" FROM P_STOCKB S, P_PRODUCTO P " +
				" WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)" +
				" AND S.BARRA NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN " +
				" (SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S'))";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaCantB=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT IFNULL(COUNT(S.CODIGO),0) AS CANTUNI " +
				" FROM P_STOCK_PALLET S, P_PRODUCTO P " +
				" WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)" +
				" AND S.BARRAPRODUCTO NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN " +
				" (SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S'))";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaCantB+=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT SUM(S.CANT) AS CANTUNI " +
				" FROM P_STOCK S, P_PRODUCTO P " +
				" WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO AND S.CANT > 0";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaCant=DT.getDouble(0);
		}

		DT.close();

		sql = " SELECT SUM(S.CANT) AS CANTUNI " +
			  " FROM P_STOCK_PALLET S, P_PRODUCTO P " +
			  " WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO AND S.CANT > 0";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0) {
			DT.moveToFirst();
			sumaCant+=DT.getDouble(0);
		}

		DT.close();

		sumaCant = sumaCant + sumaCantB;

		return sumaCant;
	}

	public String umVenta(String cod) {
		Cursor DT;
		String umm;

		try {
			String sql = "SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE CODIGO='" + cod + "' AND NIVEL="+gl.nivel;
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			umm=DT.getString(0);
			return  umm;
		} catch (Exception e) {
			toast(e.getMessage());
			return "";
		}
	}

	public String umStock(String cod) {
		Cursor DT;
		String umm,sql;

		try {
			sql = "SELECT UNIDADMEDIDA FROM P_STOCK WHERE CODIGO='"+cod+ "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount()==0) {
				sql = "SELECT UNIDADMEDIDA FROM P_STOCKB WHERE CODIGO='"+cod+ "'";
				DT = Con.OpenDT(sql);
			}

			DT.moveToFirst();
			umm=DT.getString(0);

			return  umm;
		} catch (Exception e) {
			toast(e.getMessage());
			return "";
		}
	}

	public double factorPeso(String cod) {
		Cursor DT;

		try {
			String sql = "SELECT PESO_PROMEDIO FROM P_PRODUCTO WHERE CODIGO='" + cod + "'";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			return  DT.getDouble(0);
		} catch (Exception e) {
			toast(e.getMessage());
			return 0;
		}
	}

	public double factorPres(String cod,String umventa,String umstock) {
		Cursor DT;
		String sql;

		try {
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
				"WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+umventa+"') AND (UNIDADMINIMA='"+umstock+"')";
			DT = Con.OpenDT(sql);

			if (DT.getCount()==0) {
				sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
					"WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+umventa+"')";
				DT = Con.OpenDT(sql);
			}

			DT.moveToFirst();

			return  DT.getDouble(0);
		} catch (Exception e) {
			return 1;
		}
	}

	public boolean validaImpresora() {
		CryptUtil cu=new CryptUtil();
		Cursor dt;
		String se,sd,prid;

		try {
			sql = "SELECT prnserie FROM Params";
			dt = Con.OpenDT(sql);
			dt.moveToFirst();

			prid = dt.getString(0);
		} catch (Exception e) {
			return false;
		}

		try {

			sql="SELECT NUMSERIE FROM P_IMPRESORA";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) dt.moveToFirst();
			while (!dt.isAfterLast()) {
				se=dt.getString(0);
				//sd=cu.decrypt(se);
				sd=se;

				if (sd.equalsIgnoreCase(prid)) return true;

				dt.moveToNext();
			}
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return false;

	}

	public String impresTipo() {
		Cursor dt;
		String prnid;

		try {

			sql="SELECT prn FROM Params";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			prnid=dt.getString(0);

			sql="SELECT MARCA FROM P_IMPRESORA WHERE IDIMPRESORA='"+prnid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return "SIN IMPRESORA";
			dt.moveToFirst();

			return dt.getString(0);

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			return "SIN IMPRESORA";
		}
	}

	public String impresParam() {
		CryptUtil cu=new CryptUtil();
		Cursor dt;
		String prnid;

		try {

			sql="SELECT prn FROM Params";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			prnid=dt.getString(0);

			sql="SELECT MACADDRESS FROM P_IMPRESORA WHERE IDIMPRESORA='"+prnid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return " #### ";
			dt.moveToFirst();

			return cu.decrypt(dt.getString(0));

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			return " #### ";
		}
	}

	public long fechaFactTol(long f0) {
		Cursor DT;
		String sql;
		long ff;

		try {
			sql = "SELECT FECHA FROM P_FECHA";
			DT = Con.OpenDT(sql);

			DT.moveToFirst();
			ff=DT.getLong(0);

			return ff;
		} catch (Exception e) {
			return f0;
		}
	}


	// Common
	
	protected void toast(String msg) {
		Toast toast= Toast.makeText(cont,msg, Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}

	private boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}

	public void msgbox(String msg) {

		try{

			if (!emptystr(msg)){

				AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

				dialog.setTitle(R.string.app_name);
				dialog.setMessage(msg);

				dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//Toast.makeText(getApplicationContext(), "Yes button pressed",Toast.LENGTH_SHORT).show();
					}
				});
				dialog.show();

			}

		}catch (Exception ex)
			{toast(ex.getMessage());}
	}

}
