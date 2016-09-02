package com.example.synerzip.quiz;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by synerzip on 4/8/16.
 */
public class Mail extends javax.mail.Authenticator {
    private String _user;
    private String _pass;
    private String[] _to;
    private String _from;
    private String _port;
    private String _sport;

    private String _host;
    private boolean _auth;
    private boolean _debuggable;
    private Multipart _multipart;

    private String _subject;
    private String _body;

    private static final String HOST="smtp.gmail.com";
    private static final String PORT="465";
    private static final String MAIL_CAP_HTML="text/html;; x-java-content-handler=com.sun.mail.handlers.text_html";
    private static final String MAIL_CAP_XML="text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml";
    private static final String MAIL_CAP_PLAIN="text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain";
    private static final String MAIL_CAP_MIXED="multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed";
    private static final String MAIL_CAP_RFC="message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822";
    private static final String SMTP="smtp";
    private static final String _HOST="mail.smtp.host";
    private static final String DEBUG_KEY="mail.debug";
    private static final String AUTH_KEY="mail.smtp.auth";
    private static final String _PORT="mail.smtp.port";
    private static final String SPORT="mail.smtp.socketFactory.port";
    private static final String SOCKET_FACTORY_KEY="mail.smtp.socketFactory.class";
    private static final String SOCKET_FACTORY_VALUE= "javax.net.ssl.SSLSocketFactory";
    private static final String OBJECT_KEY="mail.smtp.socketFactory.fallback";
    private static final String OBJECT_VALUE="false";



    public String[] get_to() {
        return _to;
    }

    public void set_to(String[] _to) {
        this._to = _to;
    }

    public String get_body() {
        return _body;
    }

    public void set_body(String _body) {
        this._body = _body;
    }

    public String get_subject() {
        return _subject;
    }

    public void set_subject(String _subject) {
        this._subject = _subject;
    }

    public Multipart get_multipart() {
        return _multipart;
    }

    public void set_multipart(Multipart _multipart) {
        this._multipart = _multipart;
    }

    public boolean is_debuggable() {
        return _debuggable;
    }

    public void set_debuggable(boolean _debuggable) {
        this._debuggable = _debuggable;
    }

    public boolean is_auth() {
        return _auth;
    }

    public void set_auth(boolean _auth) {
        this._auth = _auth;
    }

    public String get_host() {
        return _host;
    }

    public void set_host(String _host) {
        this._host = _host;
    }

    public String get_sport() {
        return _sport;
    }

    public void set_sport(String _sport) {
        this._sport = _sport;
    }

    public String get_port() {
        return _port;
    }

    public void set_port(String _port) {
        this._port = _port;
    }

    public String get_from() {
        return _from;
    }

    public void set_from(String _from) {
        this._from = _from;
    }

    public String get_pass() {
        return _pass;
    }

    public void set_pass(String _pass) {
        this._pass = _pass;
    }

    public String get_user() {
        return _user;
    }

    public void set_user(String _user) {
        this._user = _user;
    }

    public Mail(){
        _host= HOST; //default smtp server
        _port= PORT;  //default smtp port 465
        _sport= PORT;  //default socketFactory port 587
        _user="";  //username
        _pass="";  //password
        _from=""; //email sent from
        _subject="";  //email subject
        _body="";  //email body
        _debuggable=false;  // debug mode on or off - default off
        _auth=true;  //smtp authentication - default on

        _multipart=new MimeMultipart();

        MailcapCommandMap map= (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        map.addMailcap(MAIL_CAP_HTML);
        map.addMailcap(MAIL_CAP_XML);
        map.addMailcap(MAIL_CAP_PLAIN);
        map.addMailcap(MAIL_CAP_MIXED);
        map.addMailcap(MAIL_CAP_RFC);
        CommandMap.setDefaultCommandMap(map);
    }

    public Mail(String user,String pass){
        this();
        _user = user;
        _pass = pass;
    }

    public boolean send() throws Exception{
        Properties properties=setProperties();

        if(!_user.equals("") && !_pass.equals("") && _to.length>0 && !_subject.equals("") && !_body.equals("")){
            Session session=Session.getDefaultInstance(properties,this);
            MimeMessage msg=new MimeMessage(session);
//            msg.setFrom(new InternetAddress(_from,false));

            InternetAddress[] addressTo=new InternetAddress[_to.length];
            for (int i=0;i<_to.length;i++){
                addressTo[i]=new InternetAddress(_to[i]);
            }

            msg.setRecipients(MimeMessage.RecipientType.TO,addressTo);
            msg.setSubject(_subject);
            msg.setSentDate(new Date());

            //Setup Message body
            BodyPart msgBodyPart=new MimeBodyPart();
            msgBodyPart.setText(_body);
            _multipart.addBodyPart(msgBodyPart);

            msg.setContent(_multipart);
            Transport transport=session.getTransport(SMTP);
            transport.connect(_host,_user,_pass);
            transport.send(msg);

            return true;
        }else {
            return false;
        }
    }
    public void addAttachment(String filePath,String fileName) throws Exception{
        BodyPart msgBodyPart=new MimeBodyPart();
        DataSource source=new FileDataSource(filePath);
        msgBodyPart.setDataHandler(new DataHandler(source));
        msgBodyPart.setFileName(fileName);
        _multipart.addBodyPart(msgBodyPart);

    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(_user, _pass);
    }

    private Properties setProperties() {
        Properties pro=new Properties();
        pro.put(_HOST, _host);

        if(_debuggable) {
            pro.put(DEBUG_KEY, "true");
        }
        if(_auth) {
            pro.put(AUTH_KEY, "true");
        }

        pro.put(_PORT, _port);
        pro.put(SPORT, _sport);
        pro.put(SOCKET_FACTORY_KEY, SOCKET_FACTORY_VALUE);
        pro.put(OBJECT_KEY, OBJECT_VALUE);

        return pro;
    }


}
