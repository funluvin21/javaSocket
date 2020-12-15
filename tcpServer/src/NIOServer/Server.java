package NIOServer;

//import java.io.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Server implements Runnable {
    private InetSocketAddress listenAddress;
    // 메시지는 개행으로 구분한다.
    private static char CR = (char) 0x0D;
    private static char LF = (char) 0x0A;
    private static int stHeaderLen = 60;
    
    // ip와 port 설정
    public Server(String address, int port) {
        listenAddress = new InetSocketAddress(port);
    }
	
     // Thread 실행.
    public void run() {
        // 셀렉터 설정
        try (Selector selector = Selector.open()) {
            // 채널 설정
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                // non-Blocking 설정
                serverChannel.configureBlocking(false);
                // 서버 ip, port 설정
                serverChannel.socket().bind(listenAddress);
                // 채널에 accept 대기 설정
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                // 셀렉터가 있을 경우.
                while (selector.select() > 0) {
                    // 셀렉터 키 셋를 가져온다.
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    // 키가 있으면..
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                           //키 셋에서 제거.
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                            }

                           // 접속일 경우..
                        if (key.isAcceptable()) {
                            this.accept(selector, key);
                            // 수신일 경우..
                        } else if (key.isReadable()) {
                            this.receive(selector, key);
                            // 발신일 경우..
                        } else if (key.isWritable()) {
                            this.send(selector, key);
                            }
                    }
                 }
              }
         } catch (IOException e) {
            e.printStackTrace();
           }
    }
	
    // 접속시 호출 함수..
    private void accept(Selector selector, SelectionKey key) {
        try {
            // 키 채널을 가져온다.
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            // accept을 해서 Socket 채널을 가져온다.
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            // 	소켓 취득
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connected to: " + remoteAddr);
            // 	접속 Socket 단위로 사용되는 Buffer;
            StringBuffer sb = new StringBuffer();
            sb.append("Welcome server!\r\n>");
            // Socket 채널을 channel에 송신 등록한다
            channel.register(selector, SelectionKey.OP_WRITE, sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 수신시 호출 함수..
    private void receive(Selector selector, SelectionKey key) {
        try {
             // 키 채널을 가져온다.
           SocketChannel channel = (SocketChannel) key.channel();
             // 채널 Non-blocking 설정
           channel.configureBlocking(false);
             // 소켓 취득
           Socket socket = channel.socket();
           // Byte 버퍼 생성
           ByteBuffer buffer = ByteBuffer.allocate(1024);
             // ***데이터 수신***
           int size = channel.read(buffer);

            //수신 크기가 없으면 소켓 접속 종료.
           if (size == -1) {
			    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
				System.out.println("Connection closed by client: " + remoteAddr);
				// 소켓 채널 닫기
				channel.close();
				// 소켓 닫기
				socket.close();
				// 키 닫기
				key.cancel();
				return;
            }
            // ByteBuffer -> byte[]
            byte[] data = new byte[size];
            System.arraycopy(buffer.array(), 0, data, 0, size);

              //헤더 및 tr 확인
              
            ST_Header st_header = new ST_Header();
            //st_header = toObject(data, st_header);
            //st_header.setStHeader(data);
            //********st_header = (ST_Header)toObject(data);
            
            byte[] t4Byte = new byte[4];
            System.arraycopy(data, 0, t4Byte, 0, 4);
            st_header.DataLen = byteArrayToInt(t4Byte);

            // StringBuffer 취득
            StringBuffer sb = (StringBuffer) key.attachment();

           //private static int stHeaderLen = 60;
           int iStrLen = st_header.DataLen - (stHeaderLen - 4);
           int iPos = stHeaderLen;

            byte[] bStrData = new byte[iStrLen];
            System.arraycopy(buffer.array(), iPos, bStrData, 0, iStrLen);
            // 버퍼에 수신된 데이터 추가
            sb.append(new String(bStrData));
            // 데이터 끝이 개행 일 경우.
            if (sb.length() > 2 && sb.charAt(sb.length() - 2) == CR && sb.charAt(sb.length() - 1) == LF) {
				// 개행 삭제
				sb.setLength(sb.length() - 2);
				// 메시지를 콘솔에 표시한다.
				String msg = sb.toString();
				System.out.println(msg);
				// exit 경우 접속을 종료한다.
				if ("exit".equals(msg)) {
					// 소켓 채널 닫기
					channel.close();
					// 소켓 닫기
					socket.close();
					// 키 닫기
					key.cancel();
					return;
				}
				// Echo - 메시지> 의 형태로 재 전송.
				sb.insert(0, "Echo - ");
				sb.append("\r\n>");
				// Socket 채널을 channel에 송신 등록한다
				channel.register(selector, SelectionKey.OP_WRITE, sb);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

/************
byte[] d_DataLen = new byte[4];
System.arraycopy(buffer.array(), 0, d_DataLen, 0, 4);
int dataLen = byte2Int(d_DataLen);
System.out.println(dataLen);

byte[] d_TrCode = new byte[4];
System.arraycopy(buffer.array(), 4, d_TrCode, 0, 4);
int trCode = byte2Int(d_TrCode);
System.out.println(trCode);

*********************/

    // 발신시 호출 함수
    private void send(Selector selector, SelectionKey key) {
        try {
             // 키 채널을 가져온다.
           SocketChannel channel = (SocketChannel) key.channel();
             // 채널 Non-blocking 설정
           channel.configureBlocking(false);
           // StringBuffer 취득
           StringBuffer sb = (StringBuffer) key.attachment();
           String data = sb.toString();
           // StringBuffer 초기화
           sb.setLength(0);
          
           //ST_Header mStHeader = new ST_Header();
           //byte[] mByteHeader = toByteArray(mStHeader);
           //mStHeader.DataLen = stHeaderLen + data.length();
           byte[] mByteHeader = new byte[stHeaderLen];
           byte[] mByteTemp = new byte[4];
           
           //DataLen
           mByteTemp = intToByteArray(stHeaderLen - 4 + data.length());
           System.arraycopy(mByteTemp, 0, mByteHeader, 0, 4);
           //TrCode
           mByteTemp = intToByteArray(2000);
           System.arraycopy(mByteTemp, 0, mByteHeader, 4, 4);
           //Dest_Way;
           mByteTemp = intToByteArray(2001);
           System.arraycopy(mByteTemp, 0, mByteHeader, 8, 4);
           //Client_Handle;
           mByteTemp = intToByteArray(2002);
           System.arraycopy(mByteTemp, 0, mByteHeader, 12, 4);
           //User_Field;
           mByteTemp = intToByteArray(2003);
           System.arraycopy(mByteTemp, 0, mByteHeader, 16, 4);
           //Data_Type;
           mByteTemp = intToByteArray(2004);
           System.arraycopy(mByteTemp, 0, mByteHeader, 20, 4);
           //Client_Rtn1;
           mByteTemp = intToByteArray(2005);
           System.arraycopy(mByteTemp, 0, mByteHeader, 24, 4);
           //Client_Rtn2;
           mByteTemp = intToByteArray(2006);
           System.arraycopy(mByteTemp, 0, mByteHeader, 28, 4);
           //Client_Rtn3;
           mByteTemp = intToByteArray(2007);
           System.arraycopy(mByteTemp, 0, mByteHeader, 32, 4);
           //User_ID
           byte[] mByteUser = new byte[8];
           System.arraycopy(mByteUser, 0, mByteHeader, 36, 8);
           byte CompressFlg = 1;
           //System.arraycopy(CompressFlg, 0, mByteHeader, 44, 1);
           mByteHeader[44] = CompressFlg;
           byte KillGbn = 0;
           //System.arraycopy(KillGbn, 0, mByteHeader, 45, 1);
           mByteHeader[44] = KillGbn;
           byte Filler1 = 0;
           //System.arraycopy(Filler1, 0, mByteHeader, 46, 1);
           mByteHeader[44] = Filler1;
           byte Filler2 = 0;
           //System.arraycopy(Filler2, 0, mByteHeader, 47, 1);
           mByteHeader[44] = Filler2;
           byte[] mByteMsgCd = new byte[4];
           String MsgCd = "0000";
           mByteMsgCd = MsgCd.getBytes();
           System.arraycopy(mByteMsgCd, 0, mByteHeader, 48, 4);
            //Next_KeyLen
           mByteTemp = intToByteArray(0);
           System.arraycopy(mByteTemp, 0, mByteHeader, 52, 4);
           //Option_len
           mByteTemp = intToByteArray(0);
           System.arraycopy(mByteTemp, 0, mByteHeader, 56, 4);
           
           byte[] mByteData = data.getBytes();
           byte[] mByteTotal = new byte[mByteHeader.length + mByteData.length];
           //byte[] mByteTotLen = intToByteArray(stHeaderLen + data.length() + 4);
           
           //System.arraycopy(mByteTotLen, 0, mByteTotal, 0, 4);
           System.arraycopy(mByteHeader, 0, mByteTotal, 0, mByteHeader.length);
           System.arraycopy(mByteData, 0, mByteTotal, mByteHeader.length, mByteData.length);
                      
           
           //byte 형식으로 변환
           //ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
           ByteBuffer buffer = ByteBuffer.wrap(mByteTotal);

            // ***데이터 송신***
           channel.write(buffer);
           // Socket 채널을 channel에 수신 등록한다
           channel.register(selector, SelectionKey.OP_READ, sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    // 시작 함수
    public static void main(String[] args) {
        System.out.println("Start Server Listen :7777");
        // 7777포트를 Listen한다.
        Executors.newSingleThreadExecutor().execute(new Server("localhost", 7777));
    }

        public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(obj);
                        oos.flush();
                        oos.close();
                        bos.close();
                        bytes = bos.toByteArray();
                } catch (IOException e) {
            e.printStackTrace();
                }
                return bytes;
        }
/*
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
         }
        return obj;
    }
  */  
    public static <T> T toObject (byte[] bytes, Class<T> type)
    {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = ois.readObject();
        }
        catch (IOException ex) {
            //TODO: Handle the exception
        }
        catch (ClassNotFoundException ex) {
            //TODO: Handle the exception
        }
        return type.cast(obj);
    }

    public int byteArrayToInt(byte[] bt) {
        int s1 = bt[0] & 0xFF;
        int s2 = bt[1] & 0xFF;
        int s3 = bt[2] & 0xFF;
        int s4 = bt[3] & 0xFF;
        return ((s4 << 24) + (s3 << 16) + (s2 << 8) + (s1 << 0));
    }
    
    public  byte[] intToByteArray(int value) {
		byte[] byteArray = new byte[4];
		/*
		byteArray[0] = (byte)(value >> 24);
		byteArray[1] = (byte)(value >> 16);
		byteArray[2] = (byte)(value >> 8);
		byteArray[3] = (byte)(value);
		*/
		byteArray[3] = (byte)(value >> 24);
		byteArray[2] = (byte)(value >> 16);
		byteArray[1] = (byte)(value >> 8);
		byteArray[0] = (byte)(value);
		return byteArray;
	}
    
    /*
    public void setStHeader(byte[] btData) {
            this.HeaderSz = (4 * 11) + (1 * 4) + 12;
   
            byte[] t4Byte = new byte[4];
            System.arraycopy(btData, 0, t4Byte, 0, 4);
            this.DataLen = byte2Int(t4Byte);
            System.arraycopy(btData, 4, t4Byte, 0, 4);
            this.TrCode = byte2Int(t4Byte);
    }

public int byte2Int_test(byte[] bt) {
    int s1 = bt[0] & 0xFF;
    int s2 = bt[1] & 0xFF;
    int s3 = bt[2] & 0xFF;
    int s4 = bt[3] & 0xFF;

    return ((s4 << 24) + (s3 << 16) + (s2 << 8) + (s1 << 0));
}
    */
//출처: https://it77.tistory.com/47 [시원한물냉의 사람사는 이야기]
}

//Header
class ST_Header implements Serializable {
    public int DataLen;
    public int TrCode;
    public int Dest_Way;
    public int Client_Handle;
    public int User_Field;
    public int Data_Type;
    public int Client_Rtn1;
    public int Client_Rtn2;
    public int Client_Rtn3;
    public byte[] User_ID;//[8]
    //public String User_ID;
    public byte CompressFlg;
    public byte KillGbn;
    public byte Filler1;
    public byte Filler2;
    //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
    public byte[] Msg_cd;//[4]
    //  public String Msg_cd;
    public int Next_KeyLen;
    public int Option_len;
  
  
  
}
