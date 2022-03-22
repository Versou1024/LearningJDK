package sun.security.util;

import java.util.ListResourceBundle;

public class AuthResources_ko extends ListResourceBundle
{
  private static final Object[][] contents = { { "invalid null input: value", "잘못된 널 입력:  {0}" }, { "NTDomainPrincipal: name", "NTDomainPrincipal: {0}" }, { "NTNumericCredential: name", "NTNumericCredential: {0}" }, { "Invalid NTSid value", "잘못된 NTSid 값" }, { "NTSid: name", "NTSid: {0}" }, { "NTSidDomainPrincipal: name", "NTSidDomainPrincipal: {0}" }, { "NTSidGroupPrincipal: name", "NTSidGroupPrincipal: {0}" }, { "NTSidPrimaryGroupPrincipal: name", "NTSidPrimaryGroupPrincipal: {0}" }, { "NTSidUserPrincipal: name", "NTSidUserPrincipal: {0}" }, { "NTUserPrincipal: name", "NTUserPrincipal: {0}" }, { "UnixNumericGroupPrincipal [Primary Group]: name", "UnixNumericGroupPrincipal [기본 그룹]:  {0}" }, { "UnixNumericGroupPrincipal [Supplementary Group]: name", "UnixNumericGroupPrincipal [보조 그룹]:  {0}" }, { "UnixNumericUserPrincipal: name", "UnixNumericUserPrincipal: {0}" }, { "UnixPrincipal: name", "UnixPrincipal: {0}" }, { "Unable to properly expand config", "적절히 확장할 수 없습니다. {0}" }, { "extra_config (No such file or directory)", "{0} (해당 파일이나 디렉토리가 없습니다.)" }, { "Unable to locate a login configuration", "로그인 구성을 찾을 수 없습니다." }, { "Configuration Error:\n\tInvalid control flag, flag", "구성 오류:\n\t잘못된 컨트롤 플래그, {0}" }, { "Configuration Error:\n\tCan not specify multiple entries for appName", "구성 오류:\n\t{0}에 대해 여러 항목을 지정할 수 없습니다." }, { "Configuration Error:\n\texpected [expect], read [end of file]", "구성 오류:\n\t예상 [{0}], 읽음 [파일의 끝]" }, { "Configuration Error:\n\tLine line: expected [expect], found [value]", "구성 오류:\n\t줄 {0}: 예상 [{1}], 발견 [{2}]" }, { "Configuration Error:\n\tLine line: expected [expect]", "구성 오류:\n\t줄 {0}: 예상 [{1}]" }, { "Configuration Error:\n\tLine line: system property [value] expanded to empty value", "구성 오류:\n\t줄 {0}: 시스템 등록 정보 [{1}]이(가) 빈 값으로 확장되었습니다." }, { "username: ", "사용자 이름: " }, { "password: ", "암호: " }, { "Please enter keystore information", "Keystore 정보를 입력하십시오." }, { "Keystore alias: ", "Keystore 별명: " }, { "Keystore password: ", "Keystore 암호: " }, { "Private key password (optional): ", "개인 키 암호(선택 사항): " }, { "Kerberos username [[defUsername]]: ", "Kerberos 사용자 이름 [{0}]: " }, { "Kerberos password for [username]: ", "{0}의 Kerberos 암호: " }, { ": error parsing ", ": 구문 분석 오류 " }, { ": ", ": " }, { ": error adding Permission ", ": 사용 권한 추가 중 오류 " }, { " ", " " }, { ": error adding Entry ", ": 입력 항목 추가 중 오류 " }, { "(", "(" }, { ")", ")" }, { "attempt to add a Permission to a readonly PermissionCollection", "읽기 전용 PermissionCollection에 사용 권한을 추가하려고 시도했습니다." }, { "expected keystore type", "Keystore 유형이 필요합니다." }, { "can not specify Principal with a ", "와일드카드 클래스를 와일드카드 이름이 없이 " }, { "wildcard class without a wildcard name", "기본값을 지정할 수 없습니다." }, { "expected codeBase or SignedBy", "codeBase 또는 SignedBy가 필요합니다." }, { "only Principal-based grant entries permitted", "기본값 기반 부여 입력 항목만 허용됩니다." }, { "expected permission entry", "사용 권한 입력 항목이 필요합니다." }, { "number ", "숫자 " }, { "expected ", "필요합니다. " }, { ", read end of file", ", 파일의 끝을 읽었습니다." }, { "expected ';', read end of file", "';'이 필요합니다. 파일의 끝을 읽었습니다." }, { "line ", "줄 " }, { ": expected '", ":  '이 필요합니다." }, { "', found '", "', '을 찾았습니다." }, { "'", "'" }, { "SolarisNumericGroupPrincipal [Primary Group]: ", "SolarisNumericGroupPrincipal [기본 그룹]: " }, { "SolarisNumericGroupPrincipal [Supplementary Group]: ", "SolarisNumericGroupPrincipal [보조 그룹]: " }, { "SolarisNumericUserPrincipal: ", "SolarisNumericUserPrincipal: " }, { "SolarisPrincipal: ", "SolarisPrincipal: " }, { "provided null name", "제공된 널 이름" } };

  public Object[][] getContents()
  {
    return contents;
  }
}