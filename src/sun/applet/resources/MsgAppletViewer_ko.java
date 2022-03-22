package sun.applet.resources;

import java.util.ListResourceBundle;

public class MsgAppletViewer_ko extends ListResourceBundle
{
  public Object[][] getContents()
  {
    return { { "textframe.button.dismiss", "닫기" }, { "appletviewer.tool.title", "애플릿 뷰어: {0}" }, { "appletviewer.menu.applet", "애플릿 " }, { "appletviewer.menuitem.restart", "재시작" }, { "appletviewer.menuitem.reload", "재로드" }, { "appletviewer.menuitem.stop", "멈춤" }, { "appletviewer.menuitem.save", "저장..." }, { "appletviewer.menuitem.start", "시작" }, { "appletviewer.menuitem.clone", "복제..." }, { "appletviewer.menuitem.tag", "태그..." }, { "appletviewer.menuitem.info", "정보..." }, { "appletviewer.menuitem.edit", "편집" }, { "appletviewer.menuitem.encoding", "문자 인코딩" }, { "appletviewer.menuitem.print", "인쇄..." }, { "appletviewer.menuitem.props", "등록 정보..." }, { "appletviewer.menuitem.close", "닫기" }, { "appletviewer.menuitem.quit", "종료" }, { "appletviewer.label.hello", "환영합니다..." }, { "appletviewer.status.start", "애플릿 시작 중..." }, { "appletviewer.appletsave.filedialogtitle", "애플릿 일련 번호를 파일에 저장" }, { "appletviewer.appletsave.err1", "{0}에서 {1}까지 일련 번호를 지정하는 중" }, { "appletviewer.appletsave.err2", "appletSave 에서 {0} 발생" }, { "appletviewer.applettag", "태그 표시" }, { "appletviewer.applettag.textframe", "애플릿 HTML 태그" }, { "appletviewer.appletinfo.applet", "-- 애플릿 정보 없음 --" }, { "appletviewer.appletinfo.param", "-- 매개변수 정보 없음 --" }, { "appletviewer.appletinfo.textframe", "애플릿 정보" }, { "appletviewer.appletprint.fail", "인쇄 오류 발생" }, { "appletviewer.appletprint.finish", "인쇄 완료" }, { "appletviewer.appletprint.cancel", "인쇄 취소" }, { "appletviewer.appletencoding", "문자 인코딩: {0}" }, { "appletviewer.parse.warning.requiresname", "경고: 이름 속성에 <param name=... value=...> 태그가 필요합니다." }, { "appletviewer.parse.warning.paramoutside", "경고: <applet> ... </applet> 외부에 <param> 태그가 있습니다." }, { "appletviewer.parse.warning.applet.requirescode", "경고: <applet> 태그에 코드 속성이 필요합니다." }, { "appletviewer.parse.warning.applet.requiresheight", "경고: <applet> 태그에 높이 속성이 필요합니다." }, { "appletviewer.parse.warning.applet.requireswidth", "경고: <applet> 태그에 너비 속성이 필요합니다." }, { "appletviewer.parse.warning.object.requirescode", "경고: <object> 태그에 코드 속성이 필요합니다." }, { "appletviewer.parse.warning.object.requiresheight", "경고: <object> 태그에 높이 속성이 필요합니다." }, { "appletviewer.parse.warning.object.requireswidth", "경고: <object> 태그에 너비 속성이 필요합니다." }, { "appletviewer.parse.warning.embed.requirescode", "경고: <embed> 태그에 코드 속성이 필요합니다." }, { "appletviewer.parse.warning.embed.requiresheight", "경고: <embed> 태그에 높이 속성이 필요합니다." }, { "appletviewer.parse.warning.embed.requireswidth", "경고: <embed> 태그에 너비 속성이 필요합니다." }, { "appletviewer.parse.warning.appnotLongersupported", "경고: <app> 태그를 더 이상 지원하지 않습니다. 대안으로 <applet>을 사용하게 되는 대상:" }, { "appletviewer.usage", "사용법: appletviewer <options> url(s)\n\nwhere <options> 다음을 포함:\n  -debug                  Java 디버거에서 applet viewer 시작\n  -encoding <encoding>    HTML 파일을 통해 문자 인코딩 지정\n  -J<runtime flag>        Java 인터프리터에 인자 전달\n\n -J 옵션은 표준이 아니며 알림 없이 변경될 수 있습니다." }, { "appletviewer.main.err.unsupportedopt", "지원하지 않는 옵션: {0}" }, { "appletviewer.main.err.unrecognizedarg", "알 수 없는 인자: {0}" }, { "appletviewer.main.err.dupoption", "옵션 중복 사용: {0}" }, { "appletviewer.main.err.inputfile", "지정된 입력 파일이 없습니다." }, { "appletviewer.main.err.badurl", "잘못된 URL: {0} ( {1} )" }, { "appletviewer.main.err.io", "읽는 중에 I/O 예외 오류: {0}" }, { "appletviewer.main.err.readablefile", "{0}이(가) 읽을 수 있는 파일인지 확인하십시오." }, { "appletviewer.main.err.correcturl", "{0}이(가) 올바른 URL입니까?" }, { "appletviewer.main.prop.store", "AppletViewer에 대한 특정 사용자 등록 정보" }, { "appletviewer.main.err.prop.cantread", "사용자 등록 정보 파일을 읽을 수 없습니다: {0}" }, { "appletviewer.main.err.prop.cantsave", "사용자 등록 정보 파일을 저장할 수 없습니다: {0}" }, { "appletviewer.main.warn.nosecmgr", "경고: 보안 사용 불가" }, { "appletviewer.main.debug.cantfinddebug", "디버거를 찾을 수 없습니다!" }, { "appletviewer.main.debug.cantfindmain", "디버거에서 핵심 메소드를 찾을 수 없습니다!" }, { "appletviewer.main.debug.exceptionindebug", "디버거에서 예외!" }, { "appletviewer.main.debug.cantaccess", "디버거를 액세스할 수 없습니다!" }, { "appletviewer.main.nosecmgr", "경고: SecurityManager가 설치되어 있지 않습니다!" }, { "appletviewer.main.warning", "경고: 애플릿을 시작하지 않았습니다. <applet> 태그가 입력되어 있는지 확인하십시오." }, { "appletviewer.main.warn.prop.overwrite", "경고: 사용자의 요구에 일시적으로 시스템 속성을 겹쳐씁니다: 키: {0} 이전 값: {1} 새로운 값: {2}" }, { "appletviewer.main.warn.cantreadprops", "경고: AppletViewer 등록 정보 파일을 읽을 수 없습니다: {0}이(가) 기본값으로 사용됩니다." }, { "appletioexception.loadclass.throw.interrupted", "클래스 로드 중단: {0}" }, { "appletioexception.loadclass.throw.notloaded", "클래스 로드 실패: {0}" }, { "appletclassloader.loadcode.verbose", "{1}을(를) 가져오기 위해 stream을 여는 중: {0}" }, { "appletclassloader.filenotfound", "찾고 있는 파일이 없음: {0}" }, { "appletclassloader.fileformat", "로드하는 중에 파일 형식 예외 오류: {0}" }, { "appletclassloader.fileioexception", "로드하는 중에 I/O 예외 오류: {0}" }, { "appletclassloader.fileexception", "로드하는 중에 {0} 예외 오류: {1}" }, { "appletclassloader.filedeath", "로드하는 중에 {0} 삭제: {1}" }, { "appletclassloader.fileerror", "로드하는 중에 {0} 오류: {1}" }, { "appletclassloader.findclass.verbose.findclass", "{0}에서 {1} 클래스를 찾습니다." }, { "appletclassloader.findclass.verbose.openstream", "{1}을(를) 가져오기 위해 stream을 여는 중: {0}" }, { "appletclassloader.getresource.verbose.forname", "{0} 이름에 대한 AppletClassLoader.getResource" }, { "appletclassloader.getresource.verbose.found", "시스템 자원인 {0} 자원을 찾았습니다." }, { "appletclassloader.getresourceasstream.verbose", "시스템 자원인 {0} 자원을 찾았습니다." }, { "appletpanel.runloader.err", "개체 또는 코드 매개변수입니다!" }, { "appletpanel.runloader.exception", "{0}의 일련 번호를 해제하는 동안 오류가 발생했습니다." }, { "appletpanel.destroyed", "애플릿을 삭제하였습니다." }, { "appletpanel.loaded", "애플릿을 로드하였습니다." }, { "appletpanel.started", "애플릿을 시작하였습니다." }, { "appletpanel.inited", "애플릿을 초기화하였습니다." }, { "appletpanel.stopped", "애플릿을 멈추었습니다." }, { "appletpanel.disposed", "애플릿을 배열하였습니다." }, { "appletpanel.nocode", "애플릿 태그에서 CODE 매개변수를 빠뜨렸습니다." }, { "appletpanel.notfound", "load: class {0}이(가) 없습니다." }, { "appletpanel.nocreate", "load: {0}을(를) 인스턴스화할 수 없습니다." }, { "appletpanel.noconstruct", "load: {0}이(가) public이 아니거나 public 생성자를 가지고 있지 않습니다." }, { "appletpanel.death", "삭제" }, { "appletpanel.exception", "예외 오류: {0}." }, { "appletpanel.exception2", "{0} 예외 오류: {1}." }, { "appletpanel.error", "오류: {0}." }, { "appletpanel.error2", "{0} 오류: {1}." }, { "appletpanel.notloaded", "Init: 애플릿을 로드하지 않았습니다." }, { "appletpanel.notinited", "Start: 애플릿을 초기화하지 않았습니다." }, { "appletpanel.notstarted", "Stop: 애플릿을 시작하지 않았습니다." }, { "appletpanel.notstopped", "Destroy: 애플릿을 멈추지 않았습니다." }, { "appletpanel.notdestroyed", "Dispose: 애플릿을 지우지 않았습니다." }, { "appletpanel.notdisposed", "Load: 애플릿을 배열하지 않았습니다." }, { "appletpanel.bail", "Interrupted: 애플릿을 긴급 복구하는 중입니다." }, { "appletpanel.filenotfound", "찾고 있는 파일이 없음: {0}" }, { "appletpanel.fileformat", "로드하는 중에 파일 형식 예외 오류: {0}" }, { "appletpanel.fileioexception", "로드하는 중에 I/O 예외 오류: {0}" }, { "appletpanel.fileexception", "로드하는 중에 {0} 예외 오류: {1}" }, { "appletpanel.filedeath", "로드하는 중에 {0} 삭제: {1}" }, { "appletpanel.fileerror", "로드하는 중에 {0} 오류: {1}" }, { "appletpanel.badattribute.exception", "HTML 구문 분석: 잘못된 width/height 속성 값" }, { "appletillegalargumentexception.objectinputstream", "AppletObjectInputStream에는 널이 아닌 로더가 필요합니다." }, { "appletprops.title", "AppletViewer 등록 정보" }, { "appletprops.label.http.server", "Http 프록시 서버:" }, { "appletprops.label.http.proxy", "Http 프록시 포트:" }, { "appletprops.label.network", "네트워크 액세스:" }, { "appletprops.choice.network.item.none", "없음" }, { "appletprops.choice.network.item.applethost", "애플릿 호스트" }, { "appletprops.choice.network.item.unrestricted", "무제한" }, { "appletprops.label.class", "클래스 액세스" }, { "appletprops.choice.class.item.restricted", "제한" }, { "appletprops.choice.class.item.unrestricted", "무제한" }, { "appletprops.label.unsignedapplet", "서명되지 않은 애플릿 허용:" }, { "appletprops.choice.unsignedapplet.no", "아니오" }, { "appletprops.choice.unsignedapplet.yes", "예" }, { "appletprops.button.apply", "적용" }, { "appletprops.button.cancel", "취소" }, { "appletprops.button.reset", "재설정" }, { "appletprops.apply.exception", "{0} 등록 정보를 저장할 수 없습니다." }, { "appletprops.title.invalidproxy", "잘못된 입력" }, { "appletprops.label.invalidproxy", "프록시 포트는 양의 정수값이어야 합니다." }, { "appletprops.button.ok", "확인" }, { "appletprops.prop.store", "AppletViewer에 대한 특정 사용자 등록 정보" }, { "appletsecurityexception.checkcreateclassloader", "보안 예외 오류: classloader" }, { "appletsecurityexception.checkaccess.thread", "보안 예외 오류: thread" }, { "appletsecurityexception.checkaccess.threadgroup", "보안 예외 오류: threadgroup: {0}" }, { "appletsecurityexception.checkexit", "보안 예외 오류: exit: {0}" }, { "appletsecurityexception.checkexec", "보안 예외 오류: exec: {0}" }, { "appletsecurityexception.checklink", "보안 예외 오류: link: {0}" }, { "appletsecurityexception.checkpropsaccess", "보안 예외 오류: properties" }, { "appletsecurityexception.checkpropsaccess.key", "보안 예외 오류: properties access {0}" }, { "appletsecurityexception.checkread.exception1", "보안 예외 오류: {0}, {1}" }, { "appletsecurityexception.checkread.exception2", "보안 예외 오류: file.read: {0}" }, { "appletsecurityexception.checkread", "보안 예외 오류: file.read: {0} == {1}" }, { "appletsecurityexception.checkwrite.exception", "보안 예외 오류: {0}, {1}" }, { "appletsecurityexception.checkwrite", "보안 예외 오류: file.write: {0} == {1}" }, { "appletsecurityexception.checkread.fd", "보안 예외 오류: fd.read" }, { "appletsecurityexception.checkwrite.fd", "보안 예외 오류: fd.write" }, { "appletsecurityexception.checklisten", "보안 예외 오류: socket.listen: {0}" }, { "appletsecurityexception.checkaccept", "보안 예외 오류: socket.accept: {0}:{1}" }, { "appletsecurityexception.checkconnect.networknone", "보안 예외 오류: socket.connect: {0}->{1}" }, { "appletsecurityexception.checkconnect.networkhost1", "보안 예외 오류: {1}을(를) 기점으로 하여 {0}에 연결할 수 없습니다." }, { "appletsecurityexception.checkconnect.networkhost2", "보안 예외 오류: {0} 또는 {1} 호스트의 IP 주소를 해독할 수 없습니다. " }, { "appletsecurityexception.checkconnect.networkhost3", "보안 예외 오류: {0} 호스트의 IP 주소를 해독할 수 없습니다. trustProxy 등록 정보를 참조하십시오." }, { "appletsecurityexception.checkconnect", "보안 예외 오류: connect: {0}->{1}" }, { "appletsecurityexception.checkpackageaccess", "보안 예외 오류: {0} 패키지를 액세스할 수 없습니다." }, { "appletsecurityexception.checkpackagedefinition", "보안 예외 오류: {0} 패키지를 정의할 수 없습니다." }, { "appletsecurityexception.cannotsetfactory", "보안 예외 오류: factory를 설정할 수 없습니다." }, { "appletsecurityexception.checkmemberaccess", "보안 예외 오류: 항목 액세스를 확인하십시오." }, { "appletsecurityexception.checkgetprintjob", "보안 예외 오류: getPrintJob" }, { "appletsecurityexception.checksystemclipboardaccess", "보안 예외 오류: getSystemClipboard" }, { "appletsecurityexception.checkawteventqueueaccess", "보안 예외 오류: getEventQueue" }, { "appletsecurityexception.checksecurityaccess", "보안 예외 오류: security operation: {0}" }, { "appletsecurityexception.getsecuritycontext.unknown", "알 수 없는 클래스로더 유형이므로 GetContext를 확인할 수 없습니다." }, { "appletsecurityexception.checkread.unknown", "알 수 없는 클래스로더 유형이므로 {0}을 확인할 수 없습니다." }, { "appletsecurityexception.checkconnect.unknown", "알 수 없는 클래스로더 유형이므로 checking connect를 확인할 수 없습니다." } };
  }
}