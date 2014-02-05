MethodListener methodPattern=com.nearfuturelaboratory.humans.service.InstagramService\s:\sserviceRequestStatusForUserID\s(\sString\s) instanceOf= fillThis=true fillParams=true fillReturnValue=true maxTriggerCount=-1
  onenter: PrintMessage message=Entering:\sInstagramService:${METHOD}${PARAM_TYPES}\n\s\sthis:${THIS}\n\s\sparams\s(userid):${PARAMS\sTO_STRING}\n\s\suptime:${UPTIME\sms}\n\s\sdate:${DATE_TIME\sMM-dd\sHH:mm:ss} output=agentlog
  onreturn: PrintMessage message=Exiting:\sInstagramService:${METHOD}${PARAM_TYPES}\n\s\sthis:${THIS}\n\s\suptime:\s${UPTIME\sms} output=agentlog
  onexception: PrintMessage message=Uncaught\sexception\sat:\s${CLASS}:${METHOD}${PARAM_TYPES}\n\s\sthrown\sexception:\s${EXCEPTION} output=agentlog
