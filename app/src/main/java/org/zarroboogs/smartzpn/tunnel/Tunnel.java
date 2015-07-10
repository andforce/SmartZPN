package org.zarroboogs.smartzpn.tunnel;

import android.annotation.SuppressLint;

import org.zarroboogs.smartzpn.core.LocalVpnService;
import org.zarroboogs.smartzpn.core.ProxyConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public abstract class Tunnel {

	final static ByteBuffer GL_BUFFER=ByteBuffer.allocate(20000);
	public static long SessionCount;
 
    protected abstract void onConnected(ByteBuffer buffer) throws Exception;
    protected abstract boolean isTunnelEstablished();
    protected abstract void beforeSend(ByteBuffer buffer) throws Exception;
    protected abstract void afterReceived(ByteBuffer buffer) throws Exception;
    protected abstract void onDispose();
    
	private SocketChannel m_InnerChannel;
	private ByteBuffer m_SendRemainBuffer;
	private Selector m_Selector;
	private Tunnel m_BrotherTunnel;
	private boolean m_Disposed;
    private InetSocketAddress m_ServerEP;
    protected InetSocketAddress m_DestAddress;

	public Tunnel(SocketChannel innerChannel,Selector selector){
		this.m_InnerChannel=innerChannel;
		this.m_Selector=selector;
		SessionCount++;
	}
	
	public Tunnel(InetSocketAddress serverAddress,Selector selector) throws IOException{
		SocketChannel innerChannel=SocketChannel.open();
		innerChannel.configureBlocking(false);
		this.m_InnerChannel=innerChannel;
		this.m_Selector=selector;
		this.m_ServerEP=serverAddress;
		SessionCount++;
	}

	public void setBrotherTunnel(Tunnel brotherTunnel){
		m_BrotherTunnel=brotherTunnel;
	}
	
	public void connect(InetSocketAddress destAddress) throws Exception{
		if(LocalVpnService.Instance.protect(m_InnerChannel.socket())){//����socket����vpn
			m_DestAddress=destAddress;
			m_InnerChannel.register(m_Selector, SelectionKey.OP_CONNECT,this);//ע�������¼�
			m_InnerChannel.connect(m_ServerEP);//����Ŀ��
		}else {
			throw new Exception("VPN protect socket failed.");
		}
	}
  
	protected void beginReceive() throws Exception{
		if(m_InnerChannel.isBlocking()){
			m_InnerChannel.configureBlocking(false);
		}
		m_InnerChannel.register(m_Selector, SelectionKey.OP_READ,this);//ע����¼�
	}
	

	protected boolean write(ByteBuffer buffer,boolean copyRemainData) throws Exception {
		int bytesSent;
    	while (buffer.hasRemaining()) {
			bytesSent=m_InnerChannel.write(buffer);
			if(bytesSent==0){
				break;//�����ٷ����ˣ���ֹѭ��
			}
		}
    	
    	if(buffer.hasRemaining()){//���û�з������
    		if(copyRemainData){//����ʣ����ݣ�Ȼ������д���¼������д��ʱд�롣
    			//����ʣ�����
    			if(m_SendRemainBuffer==null){
    				m_SendRemainBuffer=ByteBuffer.allocate(buffer.capacity());
    			}
    			m_SendRemainBuffer.clear();
        		m_SendRemainBuffer.put(buffer);
    			m_SendRemainBuffer.flip();
    			m_InnerChannel.register(m_Selector,SelectionKey.OP_WRITE, this);//ע��д�¼�
    		}
			return false;
    	}
    	else {//���������
    		return true;
		}
	}
 
    protected void onTunnelEstablished() throws Exception{
		this.beginReceive();//��ʼ�������
		m_BrotherTunnel.beginReceive();//�ֵ�Ҳ��ʼ����ݰ�
    }

    @SuppressLint("DefaultLocale")
	public void onConnectable(){
    	try {
        	if(m_InnerChannel.finishConnect()){//���ӳɹ�
        		onConnected(GL_BUFFER);//֪ͨ����TCP�����ӣ�������Ը��Э��ʵ�����ֵȡ�
        	}else {//����ʧ��
        		LocalVpnService.Instance.writeLog("Error: connect to %s failed.",m_ServerEP);
				this.dispose();
			}
		} catch (Exception e) {
			LocalVpnService.Instance.writeLog("Error: connect to %s failed: %s", m_ServerEP,e);
			this.dispose();
		}
    }
    
	public void onReadable(SelectionKey key){
		try {
			ByteBuffer buffer=GL_BUFFER;
			buffer.clear();
			int bytesRead=m_InnerChannel.read(buffer);
			if(bytesRead>0){
				buffer.flip();
				afterReceived(buffer);//�������ദ�?���������ݡ�
				if(isTunnelEstablished()&&buffer.hasRemaining()){//����������ݣ�ת�����ֵܡ�
					m_BrotherTunnel.beforeSend(buffer);//����֮ǰ���������ദ�?���������ܵȡ�
					if(!m_BrotherTunnel.write(buffer,true)){
						key.cancel();//�ֵܳԲ����ȡ���ȡ�¼���
						if(ProxyConfig.IS_DEBUG)
							System.out.printf("%s can not read more.\n", m_ServerEP);
					}
				} 
			}else if(bytesRead<0) {
				this.dispose();//�����ѹرգ��ͷ���Դ��
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.dispose();
		}
	}

	public void onWritable(SelectionKey key){
		try {
			this.beforeSend(m_SendRemainBuffer);//����֮ǰ���������ദ�?���������ܵȡ�
			if(this.write(m_SendRemainBuffer, false)) {//���ʣ������Ѿ��������
				key.cancel();//ȡ��д�¼���
				if(isTunnelEstablished()){
					m_BrotherTunnel.beginReceive();//�����ݷ�����ϣ�֪ͨ�ֵܿ���������ˡ�
				}else {
					this.beginReceive();//��ʼ���մ����������Ӧ���
				}
			}
		} catch (Exception e) {
			this.dispose();
		}
	}
	
	public void dispose(){
		disposeInternal(true);
	}
	
	void disposeInternal(boolean disposeBrother) {
		if(m_Disposed){
			return;
		}
		else {
			try {
				m_InnerChannel.close();
			} catch (Exception e) {
			}
			
			if(m_BrotherTunnel!=null&&disposeBrother){
				m_BrotherTunnel.disposeInternal(false);//���ֵܵ���ԴҲ�ͷ��ˡ�
			}

			m_InnerChannel=null;
		    m_SendRemainBuffer=null;
			m_Selector=null;
			m_BrotherTunnel=null;
			m_Disposed=true;
			SessionCount--;
			
			onDispose();
		}
	}
}
