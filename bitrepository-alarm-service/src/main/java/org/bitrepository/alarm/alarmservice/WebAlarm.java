package org.bitrepository.alarm.alarmservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;

/**
 * Class to format alarms for web display
 */
@XmlRootElement
public class WebAlarm {
    private String origDateTime;
    private AlarmCode alarmCode;
    private String alarmRaiser;
    private String alarmText;
    private String collectionID;

    public WebAlarm() {}
    
    public WebAlarm(Alarm alarm) {
        collectionID = alarm.getCollectionID() == null ? "" : alarm.getCollectionID();
        origDateTime = TimeUtils.shortDate(CalendarUtils.convertFromXMLGregorianCalendar(alarm.getOrigDateTime()));
        alarmRaiser = alarm.getAlarmRaiser();
        alarmCode = alarm.getAlarmCode();
        alarmText = alarm.getAlarmText();
        
    }

    @XmlElement(name = "OrigDateTime")
    public String getOrigDateTime() {
        return origDateTime;
    }
    
    public void setOrigDateTime(String origDateTime) {
        this.origDateTime = origDateTime;
    }
    
    @XmlElement(name = "AlarmCode")
    public AlarmCode getAlarmCode() {
        return alarmCode;
    }
    
    public void setAlarmCode(AlarmCode alarmCode) {
        this.alarmCode = alarmCode;
    }
    
    @XmlElement(name = "AlarmRaiser")
    public String getAlarmRaiser() {
        return alarmRaiser;
    }
    
    public void setAlarmRaiser(String alarmRaiser) {
        this.alarmRaiser = alarmRaiser;
    }
    
    @XmlElement(name = "AlarmText")
    public String getAlarmText() {
        return alarmText;
    }
    
    public void setAlarmText(String alarmText) {
        this.alarmText = alarmText;
    }    
    
    @XmlElement(name = "CollectionID")
    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }    
}