/**
 * Created by a3578 on 2016-12-16.
 */
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class db {
    public static Connection connect() {
        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/bigdata";
        String user = "postgres";
        String password = "admin";
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            return connection;
        }
        return connection;
    }

    public static boolean disconnect(Connection connection) {
        try {
            connection.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String[][] readLine(String path) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        int count = 0;
        try {
            String str = "";
            String str1 = "";
            fis = new FileInputStream(path);// FileInputStream
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            String[][] result= new String[getTotalLines(path)][];
            while ((str = br.readLine()) != null) {
                result[count++] = str.split(",");
            }
            return result;
            /*while ((str = br.readLine()) != null) {
                Statement statement=connection.createStatement();
                String[] tokens = str.split(",");
                String sql="INSERT INTO taxi VALUES ('"+tokens[0]+"','"+tokens[1]+"','" +
                        tokens[2]+"','"+tokens[3]+"','"+tokens[4]+"','"+tokens[5]+"','"+tokens[6]+"','"+tokens[7]+"',"+
                        tokens[8]+","+tokens[9]+","+tokens[10]+","+tokens[11]+",'"+tokens[12]+"');";
                statement.executeUpdate(sql);
                count++;
                statement.close();
            }*/
        }/*catch (SQLException e)
        {
            System.out.println(e);
            System.out.println("存入数据"+TaxiID+count+" 行时出错");
        }*/ catch (FileNotFoundException e) {
            System.out.println("找不到指定文件");
        } catch (IOException e) {
            System.out.println("读取文件失败");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static int getTotalLines(String path) throws IOException {
        FileReader in = new FileReader(path);
        LineNumberReader reader = new LineNumberReader(in);
        String strLine = reader.readLine();
        int totalLines = 0;
        while (strLine != null) {
            totalLines++;
            strLine = reader.readLine();
        }
        reader.close();
        in.close();
        return totalLines;
    }

    public static String[][] process(String rainPath,String flowPath,String siteId) throws IOException, ParseException, SQLException {

        String[][] flowResult=readLine(flowPath);
        String[][] rainResult=readLine(rainPath);
        Arrays.sort(rainResult, new Comparator<String[]>(){
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        });
        Arrays.sort(flowResult, new Comparator<String[]>(){
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[2].compareTo(o2[2]);
            }
        });
        int num=0;
        String[][] result=new String[flowResult.length][];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for(int i=0;i<rainResult.length;i++){
            try {
                if(rainResult[i][0].equals(siteId)) {
                    String rainTime = sdf.format(sdf.parse(rainResult[i][2]));
                    String nextRainTime = sdf.format(new Date(sdf.parse(rainResult[i][2]).getTime() + 600000));
                    while (num < flowResult.length && sdf.format(sdf.parse(flowResult[num][2])).compareTo(nextRainTime) < 0) {
                        result[num]=new String[2];
                        result[num][0] = rainResult[i][3];

                        result[num][1] = flowResult[num][3];
                        /*result[num][2] = flowResult[num][2];
                        result[num][3] = flowResult[num][3];
                        result[num][4] = flowResult[num][4];
                        result[num][5] = rainResult[i][3];*/
                        num++;
                    }
                }
            }catch (Exception e) {
                System.out.println(siteId+"文件第"+num+"数据出错，已跳过"+e);
                continue;
            }
        }
        System.out.println(siteId+"文件共"+num+"条数据处理并存储完成");
        return result;
    }

    public  static  void writeLine(String path,String[][]data) throws IOException {
        FileWriter fw=new FileWriter(path);
        for(int i=0;i<data.length;i++){
            fw.write(data[i][0]+",");
            for(int j=0;j<60&&(i+j)<data.length;j++){
                fw.write(data[i+j][1]);
                if(j!=59){
                    fw.write(",");
                }
            }
            fw.write("\n");
        }
        fw.flush();
        fw.close();
        System.out.println("导出文件完成");
    }
    public static void main(String[] args) throws IOException, ParseException, SQLException {
/*        Connection connection=connect();
        System.out.println("是否成功连接数据库"+connection);
*//*        String Filepath="E:\\university\\project\\基于大数据分析的天气与城市交通相关性研究\\documents\\数据";
        File file = new File(Filepath);
        String [] fileName = file.list();
        for (int i=0;i<fileName.length;i++) {
            String fileType=fileName[i].substring(fileName[i].length()-3,fileName[i].length());
            if(fileType.equals("txt")){
                //readLine(fileName[i],connection);
                process(fileName[i],connection);
            }
        }*//*
*//*        try {
            Statement stm = connection.createStatement();
            ResultSet rs=stm.executeQuery("select * from taxi where \"Taxiid\"='00023'");
        } catch (SQLException e) {
            e.printStackTrace();
        }*//*
        //process("taxiData00003.txt",connection);
        boolean isDiscon=disconnect(connection);
        System.out.println("是否成功关闭数据库"+isDiscon);*/
        String siteId="17";
        String savePath="E:\\result"+siteId+".txt";
        String rainPath="E:\\rain.txt";
        String flowPath="E:\\flow"+siteId+".txt";
        String[][]result=process(rainPath,flowPath,siteId);
        writeLine(savePath,result);
    }
}
