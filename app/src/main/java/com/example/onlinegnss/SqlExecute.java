package com.example.onlinegnss;

import android.location.GnssCapabilities;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.os.Build;
import android.text.BoringLayout;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SqlExecute {
    private final String url="jdbc:postgresql://2.tcp.nas.cpolar.cn:13595/postgres";
    // private String url="jdbc:postgresql://192.168.1.100:5432/postgres";
    private final String user="postgres";
    private final String passwd="unip-sql";
    private final String DRIVER_NAME = "org.postgresql.Driver";
    public Connection connection=null;
    public Boolean isConnected=false;


    public String results="";

    public String errorLog="";
    public String tableName="";

    public Boolean connectSql(){
        Boolean res=false;
        try{
            Class.forName(DRIVER_NAME);
            connection= DriverManager.getConnection(url,user,passwd);


//            Statement stmt=connection.createStatement();

//            String sql="select * from phone_gnss;";
//            ResultSet resultSet=stmt.executeQuery(sql);
//
//            while (resultSet.next()){
//                int utcTimeMillis=resultSet.getInt("utcTimeMillis");
//                int TimeNanos=resultSet.getInt("TimeNanos");
//                int LeapSecond=resultSet.getInt("LeapSecond");
//                float TimeUncertaintyNanos=resultSet.getFloat("TimeUncertaintyNanos");
//                int FullBiasNanos=resultSet.getInt("FullBiasNanos");
//                float BiasNanos=resultSet.getFloat("BiasNanos");
//                float BiasUncertaintyNanos=resultSet.getFloat("BiasUncertaintyNanos");
//                float DriftNanosPerSecond=resultSet.getFloat("DriftNanosPerSecond");
//                float DriftUncertaintyNanosPerSecond=resultSet.getFloat("DriftUncertaintyNanosPerSecond");
//                String CodeType=resultSet.getString("CodeType");
//
//                Log.d("Log_Debug",String.format("%d,%d,%d,%f,%d,%f,%f,%f,%f,%s",
//                        utcTimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,
//                        FullBiasNanos,BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,
//                        CodeType));
//
//                results+=String.format("%d,%d,%d,%f,%d,%f,%f,%f,%f,%s\n",
//                        utcTimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,
//                        FullBiasNanos,BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,
//                        CodeType);
//            }

            // stmt.close();
            isConnected=true;
            res=true;
            errorLog="";
        }catch (Exception e){
            Log.e("SqlConnect",e.toString());
            errorLog=e.toString();
        }

        return res;
    }

    public Boolean disConnectSql(){

        Boolean res=false;
        try{
            connection.close();
            connection=null;
            isConnected=false;
            results="";
            res=true;
            errorLog="";
        }catch (Exception e){
            Log.e("DisConnect",e.toString());
            errorLog=e.toString();
        }
        return res;
    }

    public Boolean createTable(){
        String sql="";

        tableName= Build.ID+calendarStr();

        String tmpTableName="";
        for(int i=0;i<tableName.length();i++){
            if((tableName.charAt(i)>='a'&&tableName.charAt(i)<='z')||
                    (tableName.charAt(i)>='A'&&tableName.charAt(i)<='Z')||
                    (tableName.charAt(i)>='0'&&tableName.charAt(i)<='9')){
                tmpTableName+=tableName.charAt(i);
            }
        }

        tableName=tmpTableName;
        sql=String.format("create table %s(\n" +
                "    utcTimeMillis FLOAT NULL ,\n" +
                "    TimeNanos FLOAT NULL ,\n" +
                "    LeapSecond FLOAT NULL,\n" +
                "    TimeUncertaintyNanos FLOAT NULL ,\n" +
                "    FullBiasNanos FLOAT NULL ,\n" +
                "    BiasNanos FLOAT NULL ,\n" +
                "    BiasUncertaintyNanos FLOAT NULL ,\n" +
                "    DriftNanosPerSecond FLOAT NULL ,\n" +
                "    DriftUncertaintyNanosPerSecond FLOAT NULL ,\n" +
                "    HardwareClockDiscontinuityCount FLOAT NULL ,\n" +
                "    Svid FLOAT NULL ,\n" +
                "    TimeOffsetNanos FLOAT NULL ,\n" +
                "    State FLOAT NULL ,\n" +
                "    ReceivedSvTimeNanos FLOAT NULL ,\n" +
                "    ReceivedSvTimeUncertaintyNanos FLOAT NULL ,\n" +
                "    Cn0DbHz FLOAT NULL ,\n" +
                "    PseudorangeRateMetersPerSecond FLOAT NULL ,\n" +
                "    PseudorangeRateUncertaintyMetersPerSecond FLOAT NULL ,\n" +
                "    AccumulatedDeltaRangeState FLOAT NULL ,\n" +
                "    AccumulatedDeltaRangeMeters FLOAT NULL ,\n" +
                "    AccumulatedDeltaRangeUncertaintyMeters FLOAT NULL ,\n" +
                "    CarrierFrequencyHz FLOAT NULL ,\n" +
                "    CarrierCycles FLOAT NULL ,\n" +
                "    CarrierPhase FLOAT NULL ,\n" +
                "    CarrierPhaseUncertainty FLOAT NULL ,\n" +
                "    MultipathIndicator FLOAT NULL ,\n" +
                "    SnrInDb FLOAT NULL ,\n" +
                "    ConstellationType FLOAT NULL ,\n" +
                "    AgcDb FLOAT NULL ,\n" +
                "    BasebandCn0DbHz FLOAT NULL ,\n" +
                "    FullInterSignalBiasNanos FLOAT NULL ,\n" +
                "    FullInterSignalBiasUncertaintyNanos FLOAT NULL ,\n" +
                "    SatelliteInterSignalBiasNanos FLOAT NULL ,\n" +
                "    SatelliteInterSignalBiasUncertaintyNanos FLOAT NULL ,\n" +
                "    CodeType CHAR NULL ,\n" +
                "    IsFullTracking INT NULL\n" +
                ");",tableName);
        try{
            Statement stmt=connection.createStatement();
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e){
            errorLog=e.toString();
            return false;
        }


        return true;
    }

    public String calendarStr(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        Date date=new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    // @RequiresApi(api = Build.VERSION_CODES.R)
    public Boolean insertData(GnssMeasurementsEvent gnssMeasurementsEvent){
        if(gnssMeasurementsEvent.getMeasurements().size()==0){
            errorLog="Has No GNSS Measurement";
            return false;
        }
        String sql=String.format("INSERT INTO %s\n" +
                "    (utctimemillis,\n" +
                "     timenanos,\n" +
                "     leapsecond,\n" +
                "     timeuncertaintynanos,\n" +
                "     fullbiasnanos,\n" +
                "     biasnanos,\n" +
                "     biasuncertaintynanos,\n" +
                "     driftnanospersecond,\n" +
                "     driftuncertaintynanospersecond,\n" +
                "     hardwareclockdiscontinuitycount,\n" +
                "     svid,\n" +
                "     timeoffsetnanos,\n" +
                "     state,\n" +
                "     receivedsvtimenanos,\n" +
                "     receivedsvtimeuncertaintynanos,\n" +
                "     cn0dbhz,\n" +
                "     pseudorangeratemeterspersecond,\n" +
                "     pseudorangerateuncertaintymeterspersecond,\n" +
                "     accumulateddeltarangestate,\n" +
                "     accumulateddeltarangemeters,\n" +
                "     accumulateddeltarangeuncertaintymeters,\n" +
                "     carrierfrequencyhz,\n" +
                "     carriercycles,\n" +
                "     carrierphase,\n" +
                "     carrierphaseuncertainty,\n" +
                "     multipathindicator,\n" +
                "     snrindb,\n" +
                "     constellationtype,\n" +
                "     agcdb,\n" +
                "     basebandcn0dbhz,\n" +
                "     fullintersignalbiasnanos,\n" +
                "     fullintersignalbiasuncertaintynanos,\n" +
                "     satelliteintersignalbiasnanos,\n" +
                "     satelliteintersignalbiasuncertaintynanos,\n" +
                "     codetype,\n" +
                "     isfulltracking) VALUES",tableName);
        GnssClock gnssClock= gnssMeasurementsEvent.getClock();
        long utctimemillis=System.currentTimeMillis();
        long timenanos=gnssClock.getTimeNanos();
        int leapsecond=gnssClock.getLeapSecond();
        double timeuncertaintynanos=gnssClock.getTimeUncertaintyNanos();
        long fullbiasnanos=gnssClock.getFullBiasNanos();
        double biasnanos=gnssClock.getBiasNanos();
        double biasuncertaintynanos=gnssClock.getBiasUncertaintyNanos();
        double driftnanospersecond=gnssClock.getDriftNanosPerSecond();
        double driftuncertaintynanospersecond=gnssClock.getDriftUncertaintyNanosPerSecond();
        int hardwareclockdiscontinuitycount=gnssClock.getHardwareClockDiscontinuityCount();
        for(GnssMeasurement measurement: gnssMeasurementsEvent.getMeasurements()){
            int svid=measurement.getSvid();
            double timeoffsetnanos=measurement.getTimeOffsetNanos();
            int state=measurement.getState();
            long receivedsvtimenanos=measurement.getReceivedSvTimeNanos();
            long receivedsvtimeuncertaintynanos=measurement.getReceivedSvTimeUncertaintyNanos();
            double cn0dbhz=measurement.getCn0DbHz();
            double pseudorangeratemeterspersecond=measurement.getPseudorangeRateMetersPerSecond();
            double pseudorangerateuncertaintymeterspersecond=measurement.getPseudorangeRateUncertaintyMetersPerSecond();
            int accumulateddeltarangestate=measurement.getAccumulatedDeltaRangeState();
            double accumulateddeltarangemeters=measurement.getAccumulatedDeltaRangeMeters();
            double accumulateddeltarangeuncertaintymeters=measurement.getAccumulatedDeltaRangeUncertaintyMeters();
            double carrierfrequencyhz=measurement.getCarrierFrequencyHz();
            if(carrierfrequencyhz!=carrierfrequencyhz){
                carrierfrequencyhz=0;
            }
            long carriercycles=measurement.getCarrierCycles();
            if(carriercycles!=carriercycles){
                carriercycles=0;
            }
            double carrierphase=measurement.getCarrierPhase();
            if(carrierphase!=carrierphase){
                carrierphase=0;
            }
            double carrierphaseuncertainty=measurement.getCarrierPhaseUncertainty();
            if(carrierphaseuncertainty!=carrierphaseuncertainty){
                carrierphaseuncertainty=0;
            }
            int multipathindicator=measurement.getMultipathIndicator();
            if(multipathindicator!=multipathindicator){
                multipathindicator=0;
            }
            double snrindb=measurement.getSnrInDb();
            if(snrindb!=snrindb){
                snrindb=0;
            }
            int constellationtype=measurement.getConstellationType();
            double agcdb=measurement.getAutomaticGainControlLevelDb();
            if(agcdb!=agcdb){
                agcdb=0;
            }
            double basebandcn0dbhz=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && measurement.hasBasebandCn0DbHz()) {
                basebandcn0dbhz=measurement.getBasebandCn0DbHz();
            }
            double fullintersignalbiasnanos=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && measurement.hasFullInterSignalBiasNanos()) {
                fullintersignalbiasnanos=measurement.getFullInterSignalBiasNanos();
            }
            double fullintersignalbiasuncertaintynanos=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && measurement.hasFullInterSignalBiasUncertaintyNanos()) {
                fullintersignalbiasuncertaintynanos=measurement.getFullInterSignalBiasUncertaintyNanos();
            }
            double satelliteintersignalbiasnanos=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && measurement.hasSatelliteInterSignalBiasNanos()) {
                satelliteintersignalbiasnanos=measurement.getSatelliteInterSignalBiasNanos();
            }
            double satelliteintersignalbiasuncertaintynanos=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && measurement.hasSatelliteInterSignalBiasUncertaintyNanos()) {
                satelliteintersignalbiasuncertaintynanos=measurement.getSatelliteInterSignalBiasUncertaintyNanos();
            }

            String codetype="";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && measurement.hasCodeType()) {
                codetype=measurement.getCodeType();
            }

            int isfulltracking=0;

            sql+=String.format("(%d,%d,%d,%f,%d,%f,%f,%f,%f,%d,%d,%f,%d,%d,%d,%f,%f,%f,%d,%f,%f,%f,%d,%f,%f,%d,%f,%d,%f,%f,%f,%f,%f,%f,'%s',%d),",
                    utctimemillis,
                    timenanos,
                    leapsecond,
                    timeuncertaintynanos,
                    fullbiasnanos,
                    biasnanos,biasuncertaintynanos,
                    driftnanospersecond,driftuncertaintynanospersecond,
                    hardwareclockdiscontinuitycount,svid,timeoffsetnanos,
                    state,receivedsvtimenanos,receivedsvtimeuncertaintynanos,
                    cn0dbhz,pseudorangeratemeterspersecond,pseudorangerateuncertaintymeterspersecond,
                    accumulateddeltarangestate,accumulateddeltarangemeters,accumulateddeltarangeuncertaintymeters,
                    carrierfrequencyhz,carriercycles,carrierphase,carrierphaseuncertainty,
                    multipathindicator,snrindb,constellationtype,agcdb,basebandcn0dbhz,
                    fullintersignalbiasnanos,fullintersignalbiasuncertaintynanos,
                    satelliteintersignalbiasnanos,satelliteintersignalbiasuncertaintynanos,
                    codetype,isfulltracking);

        }

        StringBuilder sqlBuilder=new StringBuilder(sql);
        sqlBuilder.setCharAt(sql.length()-1,';');
        sql=sqlBuilder.toString();

        try{
            Statement stmt=connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            errorLog="";
        }catch (Exception e){
            errorLog=e.toString();
            //Log.e("Insert Error",e.toString());
            // Log.e("Insert Error",sql);
            Log.e("Insert Error",gnssMeasurementsEvent.getMeasurements().size()+"");
            return false;
        }
        return true;
    }
}
