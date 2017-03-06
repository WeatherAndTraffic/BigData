import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by a3578 on 2016-12-19.
 */
public class trafficeflow {
    public static class Point{
        private double X;
        private double Y;
        Point(float X,float Y){
            this.X=X;
            this.Y=Y;
        }
        public void setPoint(double X,double Y){
            this.X=X;
            this.Y=Y;
        }
        public double getX(){
            return X;
        }
        public double getY() {
            return Y;
        }
    }
    public static void main(String[] args) throws SQLException, ParseException {
        db database=new db();
        Connection connection=database.connect();
        System.out.println("是否成功连接数据库"+connection);
        //Point point=new Point(121.198777f,31.319582f);
        //calculateFlow("2015-04-20 00:04:06","2015-04-20 08:54:07",point,connection);
        //setId(connection);
        //getRoadRainTime(connection);
        boolean isDiscon=database.disconnect(connection);
        System.out.println("是否成功关闭数据库"+isDiscon);
    }


    public static void getRoadRainTime(Connection connection) throws SQLException, ParseException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select * from \"siteRainTime\" ");
        int id=1;
        while(rs.next()){
            int siteId=rs.getInt(1);
            String siteName=rs.getString(2);
            String startTime=rs.getString(3);
            String endTime=rs.getString(4);
            Statement st=connection.createStatement();
            ResultSet rs2 = st.executeQuery("select * from \"road\" WHERE \"id\"='"+siteId+"'");
            while (rs2.next()){
                String roadName=rs2.getString(3);
                float longtitude=rs2.getFloat(4);
                float latitude=rs2.getFloat(5);
                Point point=new Point(longtitude,latitude);
                id=calculateFlow(id,siteId,siteName+"观测点"+roadName,startTime,endTime,point,connection);
            }
            st.close();
        }
    }
    public static void setId(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs=statement.executeQuery("select * from \"siteRainTime\"");
        while(rs.next()) {
            String siteName = rs.getString(2);
            int siteId = rs.getInt(1);
            Statement st=connection.createStatement();
            st.executeUpdate("update road SET \"id\"='" + siteId + "' WHERE \"siteName\"='" + siteName + "';");
            st.close();
        }
        statement.close();
    }
    public  static int calculateFlow(int id,int siteId,String info,String startTime,String endTime,Point point,Connection connection) throws SQLException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        while(startTime.compareTo(endTime)<0) {
            Date date=sdf.parse(startTime);
            date=new Date(date.getTime()+59*1000);
            String nextTime=sdf.format(date);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from traffic where \"Time\"<='"+nextTime+"'" +
                    "and \"Time\">='"+startTime+"' and \"Speed\"!=0");
            int count = 1;
            float speed=0;
            while (rs.next()) {
                float longtitude = rs.getFloat(3);
                float latitude = rs.getFloat(4);
                if (ifIn(longtitude, latitude, point)) {
                    count++;
                    speed=rs.getFloat(5)+speed;
                }
            }
            speed=speed/count;
            Statement insertSt = connection.createStatement();
            String sql = "INSERT INTO \"trafficFlow\" VALUES ('"+id+"','" +siteId+"','" + startTime + "','" + (count-1) + "','" + speed + "');";
            insertSt.executeUpdate(sql);
            insertSt.close();
            id++;
            System.out.println(info+startTime+"流量:" + (count-1) + "车"+"平均速度"+speed);
            statement.close();
            date=new Date(date.getTime()+1000);
            startTime=sdf.format(date);
        }
        return id;
    }
    public  static  boolean ifIn(float longtitude,float latitude,Point point){
        double longtitudeDiff=Math.pow((longtitude-point.getX()),2);
        double latitudeDiff=Math.pow((latitude-point.getY()),2);
        if((longtitudeDiff+latitudeDiff)<0.0108108*0.0108108){
            return true;
        }
        return false;
    }
}
