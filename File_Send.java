import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.awt.Color;

public class File_Send extends JFrame implements ActionListener{
	
//===========다른 class 자원===============
	TestClient client;

//===============GUI환경==================
	private JPanel contentPane;
	private JTextArea filepath_ta = new JTextArea();
	private JProgressBar trans_progress_prg = new JProgressBar();
	private JButton file_select_btn = new JButton("찾기");
	private JButton trans_btn = new JButton("전송");
	
//==============그 밖에 자원================
	Socket socket;
	FileSender fs;
	String target_user;
	FileDialog fileopen;
	String filename;
	String filepath;

	/*윈도우 폼에 대한 정의*/
	public File_Send() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 335, 199);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel file_select_lb = new JLabel("파일찾기");
		file_select_lb.setForeground(Color.BLACK);
		file_select_lb.setHorizontalAlignment(SwingConstants.CENTER);
		file_select_lb.setBounds(12, 13, 57, 15);
		contentPane.add(file_select_lb);
		
		
		filepath_ta.setEditable(false);
		filepath_ta.setBounds(12, 38, 202, 24);
		contentPane.add(filepath_ta);
		
		
		file_select_btn.setBounds(226, 38, 77, 23);
		contentPane.add(file_select_btn);
		
		JLabel progress_lb = new JLabel("전송상태");
		progress_lb.setHorizontalAlignment(SwingConstants.CENTER);
		progress_lb.setBounds(12, 84, 57, 15);
		contentPane.add(progress_lb);
		
		
		trans_progress_prg.setBounds(12, 109, 202, 30);
		contentPane.add(trans_progress_prg);
		
		
		trans_btn.setBounds(226, 112, 77, 23);
		contentPane.add(trans_btn);
	}
	
	private void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 335, 199);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel file_select_lb = new JLabel("파일찾기");
		file_select_lb.setForeground(Color.BLACK);
		file_select_lb.setHorizontalAlignment(SwingConstants.CENTER);
		file_select_lb.setBounds(12, 13, 57, 15);
		contentPane.add(file_select_lb);
		
		
		filepath_ta.setEditable(false);
		filepath_ta.setBounds(12, 38, 202, 24);
		contentPane.add(filepath_ta);
		
		
		file_select_btn.setBounds(226, 38, 77, 23);
		contentPane.add(file_select_btn);
		
		JLabel progress_lb = new JLabel("전송상태");
		progress_lb.setHorizontalAlignment(SwingConstants.CENTER);
		progress_lb.setBounds(12, 84, 57, 15);
		contentPane.add(progress_lb);
		
		
		trans_progress_prg.setBounds(12, 109, 202, 30);
		contentPane.add(trans_progress_prg);
		
		
		trans_btn.setBounds(226, 112, 77, 23);
		contentPane.add(trans_btn);
		this.setVisible(true);
	}
	
	
	/*활용코드*/
	public File_Send(TestClient client,String target_user) {
		this.client = client;
		this.target_user = target_user;
		init();
		addActionListener();
		addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				if (fs.isAlive()) {
					JOptionPane.showMessageDialog(null, "파일 전송이 진행 중입니다.");
				} else {
					if (socket!=null) {
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					File_Send this_fs = (File_Send)e.getWindow();
					this_fs.client.close_file_send(this_fs);
				}
			}
		});
	}
	
	private void addActionListener() {
		file_select_btn.addActionListener(this);
		trans_btn.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==file_select_btn) {
			
			select_file_showFrame();
			filepath_ta.setText(filepath);
			
		}else if (e.getSource()==trans_btn) {
			if (filepath.equals("")&&filepath==null) {
				JOptionPane.showMessageDialog(null, "파일을 선택하세요", "경고", JOptionPane.WARNING_MESSAGE);
			} else {
				try{
					//FileTransmission Server에 sendfile_connection에 연결
					socket = new Socket(client.server_ip, 7777); //sendfile_connection의 포트 번호는 7777
					System.out.println("서버에 연결되었습니다.");
					fs = new FileSender(this,socket,filename,filepath);
					fs.start();
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(null, "file server 연결 실패","알림",JOptionPane.ERROR_MESSAGE);		        }
			}
		}
		
	}
	
	private void select_file_showFrame(){
		fileopen = new FileDialog(this, "문서열기", FileDialog.LOAD);	//열기모드
		fileopen.setVisible(true);
		
		filename = fileopen.getFile();
		String filedir = fileopen.getDirectory();

		filepath = filedir+"\\"+filename;//파일경로와 파일이름은 따로 분류한다.
	}

	
	class FileSender extends Thread {
		File_Send fs;
	    Socket socket;
	    DataOutputStream dos;
	    FileInputStream fis;
	    String filename;
	    String filepath;
	    File target_file;
	    int all_size;
	    public FileSender(File_Send fs,Socket socket,String filename,String filepath) {
	    	this.fs=fs;
	        this.socket = socket;
	        this.filename = filename;
	        this.filepath = filepath;
	        try {
	        	// 데이터 전송용 스트림 생성
	            dos = new DataOutputStream(socket.getOutputStream());
	        	dos.writeUTF(client.myID+"@"+target_user+"@"+filename);//서버에 보낼 파일이름을 보낸다.
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public void run() {
	    	try {
	    		target_file = new File(filepath);
	    		all_size = (int) target_file.length();
	    		trans_progress_prg.setMaximum(all_size);
	    		fis = new FileInputStream(target_file);
	    		int len=0;
	    		int size = 0;
	    		byte[] buffer = new byte[1024];
	    		while ((len = fis.read(buffer)) != -1) {
	            	size+=len;//1024씩 계속 더한다.
	                dos.write(buffer);
	                trans_progress_prg.setValue(size);
	            }
	    		if( size >= (int)all_size ){
	    			JOptionPane.showMessageDialog(null, "파일전송이 완료 되었습니다.");
	    			client.send_message("FILETRANS_REQUEST/"+client.myID+"/"+target_user+"/"+client.myID+"@"+target_user+"@"+filename+"/"+String.valueOf(all_size));
	    			//target 대상에게 file 전송요청을 위한 message를 보낸다
				}
	    		dos.close();
				fis.close();
				socket.close();
				System.out.println("완료");
				client.close_file_send(fs);
	        }catch (IOException e) {
	        	JOptionPane.showMessageDialog(null, "파일전송 서버 연결이 끊겼습니다.");
	        }
	    }
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					File_Send frame = new File_Send();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
