package sun.security.util;

import java.util.ListResourceBundle;

public class Resources_ja extends ListResourceBundle
{
  private static final Object[][] contents = { { " ", " " }, { "  ", "  " }, { "      ", "      " }, { ", ", ", " }, { "\n", "\n" }, { "*******************************************", "*******************************************" }, { "*******************************************\n\n", "*******************************************\n\n" }, { "keytool error: ", "keytool エラー: " }, { "Illegal option:  ", "不正なオプション:  " }, { "Try keytool -help", "keytool -help を実行してみてください" }, { "Command option <flag> needs an argument.", "コマンドオプション {0} には引数が必要です。" }, { "Warning:  Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified <command> value.", "警告: PKCS12 キーストアでは、ストアのパスワードと鍵のパスワードが異なっていてはなりません。ユーザーが指定した {0} の値は無視します。" }, { "-keystore must be NONE if -storetype is {0}", "-storetype が {0} の場合 -keystore は NONE でなければなりません" }, { "Too may retries, program terminated", "再試行が多すぎます。プログラムが終了しました" }, { "-storepasswd and -keypasswd commands not supported if -storetype is {0}", "-storetype が {0} の場合 -storepasswd コマンドと -keypasswd コマンドはサポートされません" }, { "-keypasswd commands not supported if -storetype is PKCS12", "-storetype が PKCS12 の場合、-keypasswd コマンドはサポートされません" }, { "-keypass and -new can not be specified if -storetype is {0}", "-storetype が {0} の場合 -keypass と -new は指定できません" }, { "if -protected is specified, then -storepass, -keypass, and -new must not be specified", "-protected が指定されている場合、-storepass、-keypass、-new を指定することはできません" }, { "if -srcprotected is specified, then -srcstorepass and -srckeypass must not be specified", "-srcprotected を指定する場合、-srcstorepass および -srckeypass は指定できません" }, { "if keystore is not password protected, then -storepass, -keypass, and -new must not be specified", "キーストアがパスワードで保護されていない場合、-storepass、-keypass、および -new は指定できません" }, { "if source keystore is not password protected, then -srcstorepass and -srckeypass must not be specified", "ソースキーストアがパスワードで保護されていない場合、-srcstorepass と -srckeypass は指定できません" }, { "Validity must be greater than zero", "妥当性はゼロより大きくなければなりません。" }, { "provName not a provider", "{0} はプロバイダではありません。" }, { "Usage error: no command provided", "使用エラー: コマンドが指定されていません" }, { "Usage error, <arg> is not a legal command", "使用エラー: {0} は不正なコマンドです" }, { "Source keystore file exists, but is empty: ", "ソースキーストアファイルは、存在しますが空です: " }, { "Please specify -srckeystore", "-srckeystore を指定してください" }, { "Must not specify both -v and -rfc with 'list' command", "'list' コマンドに -v と -rfc の両方を指定することはできません。" }, { "Key password must be at least 6 characters", "鍵のパスワードは 6 文字以上でなければなりません。" }, { "New password must be at least 6 characters", "新規パスワードは 6 文字以上でなければなりません。" }, { "Keystore file exists, but is empty: ", "キーストアファイルは存在しますが、空です: " }, { "Keystore file does not exist: ", "キーストアファイルは存在しません: " }, { "Must specify destination alias", "宛先の別名を指定する必要があります。" }, { "Must specify alias", "別名を指定する必要があります。" }, { "Keystore password must be at least 6 characters", "キーストアのパスワードは 6 文字以上でなければなりません。" }, { "Enter keystore password:  ", "キーストアのパスワードを入力してください:  " }, { "Enter source keystore password:  ", "ソースキーストアのパスワードを入力してください:  " }, { "Enter destination keystore password:  ", "出力先キーストアのパスワードを入力してください:  " }, { "Keystore password is too short - must be at least 6 characters", "キーストアのパスワードが短過ぎます - 6 文字以上にしてください。" }, { "Unknown Entry Type", "未知のエントリタイプ" }, { "Too many failures. Alias not changed", "障害が多すぎます。別名は変更されません" }, { "Entry for alias <alias> successfully imported.", "別名 {0} のエントリのインポートに成功しました。" }, { "Entry for alias <alias> not imported.", "別名 {0} のエントリはインポートされませんでした。" }, { "Problem importing entry for alias <alias>: <exception>.\nEntry for alias <alias> not imported.", "別名 {0} のエントリのインポート中に問題が発生しました: {1}。\n別名 {0} のエントリはインポートされませんでした。" }, { "Import command completed:  <ok> entries successfully imported, <fail> entries failed or cancelled", "インポートコマンドが完了しました:  {0} 件のエントリのインポートが成功しました。{1} 件のエントリのインポートが失敗したか取り消されました" }, { "Warning: Overwriting existing alias <alias> in destination keystore", "警告: 出力先キーストア内の既存の別名 {0} を上書きしています" }, { "Existing entry alias <alias> exists, overwrite? [no]:  ", "既存のエントリの別名 {0} が存在しています。上書きしますか? [no]:  " }, { "Too many failures - try later", "障害が多過ぎます - 後で実行してください。" }, { "Certification request stored in file <filename>", "証明書要求がファイル <{0}> に保存されました。" }, { "Submit this to your CA", "これを CA に提出してください。" }, { "if alias not specified, destalias, srckeypass, and destkeypass must not be specified", "別名を指定しない場合、出力先キーストアの別名、ソースキーストアのパスワード、および出力先キーストアのパスワードは指定できません" }, { "Certificate stored in file <filename>", "証明書がファイル <{0}> に保存されました。" }, { "Certificate reply was installed in keystore", "証明書応答がキーストアにインストールされました。" }, { "Certificate reply was not installed in keystore", "証明書応答がキーストアにインストールされませんでした。" }, { "Certificate was added to keystore", "証明書がキーストアに追加されました。" }, { "Certificate was not added to keystore", "証明書がキーストアに追加されませんでした。" }, { "[Storing ksfname]", "[{0} を格納中]" }, { "alias has no public key (certificate)", "{0} には公開鍵 (証明書) がありません。" }, { "Cannot derive signature algorithm", "署名アルゴリズムを取得できません。" }, { "Alias <alias> does not exist", "別名 <{0}> は存在しません。" }, { "Alias <alias> has no certificate", "別名 <{0}> は証明書を保持しません。" }, { "Key pair not generated, alias <alias> already exists", "鍵ペアは生成されませんでした。別名 <{0}> はすでに存在します。" }, { "Cannot derive signature algorithm", "署名アルゴリズムを取得できません。" }, { "Generating keysize bit keyAlgName key pair and self-signed certificate (sigAlgName) with a validity of validality days\n\tfor: x500Name", "{3} 日間有効な {0} ビットの {1} の鍵ペアと自己署名型証明書 ({2}) を生成しています\n\tディレクトリ名: {4}" }, { "Enter key password for <alias>", "<{0}> の鍵パスワードを入力してください。" }, { "\t(RETURN if same as keystore password):  ", "\t(キーストアのパスワードと同じ場合は RETURN を押してください):  " }, { "Key password is too short - must be at least 6 characters", "鍵パスワードが短過ぎます - 6 文字以上を指定してください。" }, { "Too many failures - key not added to keystore", "障害が多過ぎます - 鍵はキーストアに追加されませんでした" }, { "Destination alias <dest> already exists", "宛先の別名 <{0}> はすでに存在します。" }, { "Password is too short - must be at least 6 characters", "パスワードが短過ぎます - 6 文字以上を指定してください。" }, { "Too many failures. Key entry not cloned", "障害が多過ぎます。鍵エントリは複製されませんでした。" }, { "key password for <alias>", "<{0}> の鍵のパスワード" }, { "Keystore entry for <id.getName()> already exists", "<{0}> のキーストアエントリはすでに存在します。" }, { "Creating keystore entry for <id.getName()> ...", "<{0}> のキーストアエントリを作成中..." }, { "No entries from identity database added", "アイデンティティデータベースから追加されたエントリはありません。" }, { "Alias name: alias", "別名: {0}" }, { "Creation date: keyStore.getCreationDate(alias)", "作成日: {0,date}" }, { "alias, keyStore.getCreationDate(alias), ", "{0}, {1,date}, " }, { "alias, ", "{0}, " }, { "Entry type: <type>", "エントリタイプ: {0}" }, { "Certificate chain length: ", "証明連鎖の長さ: " }, { "Certificate[(i + 1)]:", "証明書[{0,number,integer}]:" }, { "Certificate fingerprint (MD5): ", "証明書のフィンガープリント (MD5): " }, { "Entry type: trustedCertEntry\n", "エントリのタイプ: trustedCertEntry\n" }, { "trustedCertEntry,", "trustedCertEntry," }, { "Keystore type: ", "キーストアのタイプ: " }, { "Keystore provider: ", "キーストアのプロバイダ: " }, { "Your keystore contains keyStore.size() entry", "キーストアには {0,number,integer} エントリが含まれます。" }, { "Your keystore contains keyStore.size() entries", "キーストアには {0,number,integer} エントリが含まれます。" }, { "Failed to parse input", "入力の構文解析に失敗しました。" }, { "Empty input", "入力がありません。" }, { "Not X.509 certificate", "X.509 証明書ではありません。" }, { "Cannot derive signature algorithm", "署名アルゴリズムを取得できません。" }, { "alias has no public key", "{0} には公開鍵がありません。" }, { "alias has no X.509 certificate", "{0} には X.509 証明書がありません。" }, { "New certificate (self-signed):", "新しい証明書 (自己署名型):" }, { "Reply has no certificates", "応答には証明書がありません。" }, { "Certificate not imported, alias <alias> already exists", "証明書はインポートされませんでした。別名 <{0}> はすでに存在します。" }, { "Input not an X.509 certificate", "入力は X.509 証明書ではありません。" }, { "Certificate already exists in keystore under alias <trustalias>", "証明書は、別名 <{0}> のキーストアにすでに存在します。" }, { "Do you still want to add it? [no]:  ", "追加しますか? [no]:  " }, { "Certificate already exists in system-wide CA keystore under alias <trustalias>", "証明書は、別名 <{0}> のシステム規模の CA キーストア内にすでに存在します。" }, { "Do you still want to add it to your own keystore? [no]:  ", "キーストアに追加しますか? [no]:  " }, { "Trust this certificate? [no]:  ", "この証明書を信頼しますか? [no]:  " }, { "YES", "YES" }, { "New prompt: ", "新規 {0}: " }, { "Passwords must differ", "パスワードは異なっていなければなりません。" }, { "Re-enter new prompt: ", "新規 {0} を再入力してください: " }, { "Re-enter new password: ", "新規パスワードを再入力してください: " }, { "They don't match. Try again", "一致しません。もう一度実行してください" }, { "Enter prompt alias name:  ", "{0} の別名を入力してください:  " }, { "Enter new alias name\t(RETURN to cancel import for this entry):  ", "新しい別名を入力してください\t(このエントリのインポートを取り消す場合は RETURN を押してください):  " }, { "Enter alias name:  ", "別名を入力してください:  " }, { "\t(RETURN if same as for <otherAlias>)", "\t(<{0}> と同じ場合は RETURN を押してください)" }, { "*PATTERN* printX509Cert", "所有者: {0}\n発行者: {1}\nシリアル番号: {2}\n有効期間の開始日: {3} 終了日: {4}\n証明書のフィンガープリント:\n\t MD5:  {5}\n\t SHA1: {6}\n\t 署名アルゴリズム名: {7}\n\t バージョン: {8}" }, { "What is your first and last name?", "姓名を入力してください。" }, { "What is the name of your organizational unit?", "組織単位名を入力してください。" }, { "What is the name of your organization?", "組織名を入力してください。" }, { "What is the name of your City or Locality?", "都市名または地域名を入力してください。" }, { "What is the name of your State or Province?", "州名または地方名を入力してください。" }, { "What is the two-letter country code for this unit?", "この単位に該当する 2 文字の国番号を入力してください。" }, { "Is <name> correct?", "{0} でよろしいですか?" }, { "no", "no" }, { "yes", "yes" }, { "y", "y" }, { "  [defaultValue]:  ", "  [{0}]:  " }, { "Alias <alias> has no key", "別名 <{0}> には鍵がありません" }, { "Alias <alias> references an entry type that is not a private key entry.  The -keyclone command only supports cloning of private key entries", "別名 <{0}> が参照しているエントリタイプは非公開鍵エントリではありません。-keyclone コマンドは非公開鍵エントリの複製のみをサポートします" }, { "*****************  WARNING WARNING WARNING  *****************", "*****************  警告 警告 警告  *****************" }, { "* The integrity of the information stored in your keystore  *", "*  キーストアに保存された情報の完全性は検証されて  *" }, { "* The integrity of the information stored in the srckeystore*", "* ソースキーストアに保存された情報の完全性*" }, { "* has NOT been verified!  In order to verify its integrity, *", "*  いません!  完全性を検証するには、キーストアの   *" }, { "* you must provide your keystore password.                  *", "*  パスワードを入力する必要があります。            *" }, { "* you must provide the srckeystore password.                *", "* ソースキーストアのパスワードを入力する必要があります。                *" }, { "Certificate reply does not contain public key for <alias>", "証明書応答には、<{0}> の公開鍵は含まれません。" }, { "Incomplete certificate chain in reply", "応答した証明連鎖は不完全です。" }, { "Certificate chain in reply does not verify: ", "応答した証明連鎖は検証されていません: " }, { "Top-level certificate in reply:\n", "応答したトップレベルの証明書:\n" }, { "... is not trusted. ", "... は信頼されていません。 " }, { "Install reply anyway? [no]:  ", "応答をインストールしますか? [no]:  " }, { "NO", "NO" }, { "Public keys in reply and keystore don't match", "応答した公開鍵とキーストアが一致しません。" }, { "Certificate reply and certificate in keystore are identical", "証明書応答とキーストア内の証明書が同じです。" }, { "Failed to establish chain from reply", "応答から連鎖を確立できませんでした。" }, { "n", "n" }, { "Wrong answer, try again", "応答が間違っています。もう一度実行してください。" }, { "Secret key not generated, alias <alias> already exists", "秘密鍵は生成されませんでした。別名 <{0}> はすでに存在しています" }, { "Please provide -keysize for secret key generation", "秘密鍵の生成時には -keysize を指定してください" }, { "keytool usage:\n", "keytool の使い方:\n" }, { "Extensions: ", "拡張: " }, { "-certreq     [-v] [-protected]", "-certreq     [-v] [-protected]" }, { "\t     [-alias <alias>] [-sigalg <sigalg>]", "\t     [-alias <alias>] [-sigalg <sigalg>]" }, { "\t     [-file <csr_file>] [-keypass <keypass>]", "\t     [-file <csr_file>] [-keypass <keypass>]" }, { "\t     [-keystore <keystore>] [-storepass <storepass>]", "\t     [-keystore <keystore>] [-storepass <storepass>]" }, { "\t     [-storetype <storetype>] [-providername <name>]", "\t     [-storetype <storetype>] [-providername <name>]" }, { "\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ...", "\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..." }, { "\t     [-providerpath <pathlist>]", "\t     [-providerpath <pathlist>]" }, { "-delete      [-v] [-protected] -alias <alias>", "-delete      [-v] [-protected] -alias <alias>" }, { "-exportcert  [-v] [-rfc] [-protected]", "-exportcert  [-v] [-rfc] [-protected]" }, { "\t     [-alias <alias>] [-file <cert_file>]", "\t     [-alias <alias>] [-file <cert_file>]" }, { "-genkeypair  [-v] [-protected]", "-genkeypair  [-v] [-protected]" }, { "\t     [-alias <alias>]", "\t     [-alias <alias>]" }, { "\t     [-keyalg <keyalg>] [-keysize <keysize>]", "\t     [-keyalg <keyalg>] [-keysize <keysize>]" }, { "\t     [-sigalg <sigalg>] [-dname <dname>]", "\t     [-sigalg <sigalg>] [-dname <dname>]" }, { "\t     [-validity <valDays>] [-keypass <keypass>]", "\t     [-validity <valDays>] [-keypass <keypass>]" }, { "-genseckey   [-v] [-protected]", "-genseckey   [-v] [-protected]" }, { "-help", "-help" }, { "-importcert  [-v] [-noprompt] [-trustcacerts] [-protected]", "-importcert  [-v] [-noprompt] [-trustcacerts] [-protected]" }, { "\t     [-alias <alias>]", "\t     [-alias <alias>]" }, { "\t     [-alias <alias>] [-keypass <keypass>]", "\t     [-alias <alias>] [-keypass <keypass>]" }, { "\t     [-file <cert_file>] [-keypass <keypass>]", "\t     [-file <cert_file>] [-keypass <keypass>]" }, { "-importkeystore [-v] ", "-importkeystore [-v] " }, { "\t     [-srckeystore <srckeystore>] [-destkeystore <destkeystore>]", "\t     [-srckeystore <srckeystore>] [-destkeystore <destkeystore>]" }, { "\t     [-srcstoretype <srcstoretype>] [-deststoretype <deststoretype>]", "\t     [-srcstoretype <srcstoretype>] [-deststoretype <deststoretype>]" }, { "\t     [-srcprotected] [-destprotected]", "\t     [-srcprotected] [-destprotected]" }, { "\t     [-srcstorepass <srcstorepass>] [-deststorepass <deststorepass>]", "\t     [-srcstorepass <srcstorepass>] [-deststorepass <deststorepass>]" }, { "\t     [-srcprovidername <srcprovidername>]\n\t     [-destprovidername <destprovidername>]", "\t     [-srcprovidername <srcprovidername>]\n\t     [-destprovidername <destprovidername>]" }, { "\t     [-srcalias <srcalias> [-destalias <destalias>]", "\t     [-srcalias <srcalias> [-destalias <destalias>]" }, { "\t       [-srckeypass <srckeypass>] [-destkeypass <destkeypass>]]", "\t       [-srckeypass <srckeypass>] [-destkeypass <destkeypass>]]" }, { "\t     [-noprompt]", "\t     [-noprompt]" }, { "-changealias [-v] [-protected] -alias <alias> -destalias <destalias>", "-changealias [-v] [-protected] -alias <alias> -destalias <destalias>" }, { "\t     [-keypass <keypass>]", "\t     [-keypass <keypass>]" }, { "-keypasswd   [-v] [-alias <alias>]", "-keypasswd   [-v] [-alias <alias>]" }, { "\t     [-keypass <old_keypass>] [-new <new_keypass>]", "\t     [-keypass <old_keypass>] [-new <new_keypass>]" }, { "-list        [-v | -rfc] [-protected]", "-list        [-v | -rfc] [-protected]" }, { "\t     [-alias <alias>]", "\t     [-alias <alias>]" }, { "-printcert   [-v] [-file <cert_file>]", "-printcert   [-v] [-file <cert_file>]" }, { "\t     [-alias <alias>]", "\t     [-alias <alias>]" }, { "-storepasswd [-v] [-new <new_storepass>]", "-storepasswd [-v] [-new <new_storepass>]" }, { "Warning: A public key for alias 'signers[i]' does not exist.  Make sure a KeyStore is properly configured.", "警告: 別名 {0} の公開鍵が存在しません。キーストアが正しく設定されていることを確認してください。" }, { "Warning: Class not found: class", "警告: クラスが見つかりません: {0}" }, { "Warning: Invalid argument(s) for constructor: arg", "警告: コンストラクタの引数が無効です: {0}" }, { "Illegal Principal Type: type", "不正な主体のタイプ: {0}" }, { "Illegal option: option", "不正なオプション: {0}" }, { "Usage: policytool [options]", "使い方: policytool [options]" }, { "  [-file <file>]    policy file location", "  [-file <file>]    ポリシーファイルの場所" }, { "New", "新規" }, { "Open", "開く" }, { "Save", "保存" }, { "Save As", "別名保存" }, { "View Warning Log", "警告ログの表示" }, { "Exit", "終了" }, { "Add Policy Entry", "ポリシーエントリの追加" }, { "Edit Policy Entry", "ポリシーエントリの編集" }, { "Remove Policy Entry", "ポリシーエントリの削除" }, { "Edit", "編集" }, { "Retain", "保持" }, { "Warning: File name may include escaped backslash characters. It is not necessary to escape backslash characters (the tool escapes characters as necessary when writing the policy contents to the persistent store).\n\nClick on Retain to retain the entered name, or click on Edit to edit the name.", "Warning: File name may include escaped backslash characters. It is not necessary to escape backslash characters (the tool escapes characters as necessary when writing the policy contents to the persistent store).\n\nClick on Retain to retain the entered name, or click on Edit to edit the name." }, { "Add Public Key Alias", "公開鍵の別名を追加" }, { "Remove Public Key Alias", "公開鍵の別名を削除" }, { "File", "ファイル" }, { "KeyStore", "キーストア" }, { "Policy File:", "ポリシーファイル:" }, { "Could not open policy file: policyFile: e.toString()", "ポリシーファイルを開けませんでした: {0}: {1}" }, { "Policy Tool", "Policy Tool" }, { "Errors have occurred while opening the policy configuration.  View the Warning Log for more information.", "ポリシー構成のオープン中にエラーが発生しました。詳細は警告ログを参照してください。" }, { "Error", "エラー" }, { "OK", "了解" }, { "Status", "状態" }, { "Warning", "警告" }, { "Permission:                                                       ", "アクセス権:                                                       " }, { "Principal Type:", "主体のタイプ:" }, { "Principal Name:", "主体の名前:" }, { "Target Name:                                                    ", "ターゲット名:                                                    " }, { "Actions:                                                             ", "アクション:                                                             " }, { "OK to overwrite existing file filename?", "既存のファイル {0} に上書きしますか?" }, { "Cancel", "取消し" }, { "CodeBase:", "CodeBase:" }, { "SignedBy:", "SignedBy:" }, { "Add Principal", "主体の追加" }, { "Edit Principal", "主体の編集" }, { "Remove Principal", "主体の削除" }, { "Principals:", "主体:" }, { "  Add Permission", "  アクセス権の追加" }, { "  Edit Permission", "  アクセス権の編集" }, { "Remove Permission", "アクセス権の削除" }, { "Done", "完了" }, { "KeyStore URL:", "キーストア URL:" }, { "KeyStore Type:", "キーストアのタイプ:" }, { "KeyStore Provider:", "キーストアプロバイダ:" }, { "KeyStore Password URL:", "キーストアパスワード URL:" }, { "Principals", "主体" }, { "  Edit Principal:", "  主体の編集:" }, { "  Add New Principal:", "  主体の新規追加:" }, { "Permissions", "アクセス権" }, { "  Edit Permission:", "  アクセス権の編集:" }, { "  Add New Permission:", "  新規アクセス権の追加:" }, { "Signed By:", "署名者:" }, { "Cannot Specify Principal with a Wildcard Class without a Wildcard Name", "ワイルドカード名のないワイルドカードクラスを使って主体を指定することはできません。" }, { "Cannot Specify Principal without a Name", "名前を使わずに主体を指定することはできません。" }, { "Permission and Target Name must have a value", "アクセス権とターゲット名は、値を保持する必要があります。" }, { "Remove this Policy Entry?", "このポリシーエントリを削除しますか?" }, { "Overwrite File", "ファイルを上書きします。" }, { "Policy successfully written to filename", "ポリシーの {0} への書き込みに成功しました。" }, { "null filename", "ファイル名がありません。" }, { "Save changes?", "変更を保存しますか?" }, { "Yes", "はい" }, { "No", "いいえ" }, { "Policy Entry", "ポリシーエントリ" }, { "Save Changes", "変更を保存します。" }, { "No Policy Entry selected", "ポリシーエントリが選択されていません。" }, { "Unable to open KeyStore: ex.toString()", "キーストア {0} を開けません" }, { "No principal selected", "主体が選択されていません。" }, { "No permission selected", "アクセス権が選択されていません。" }, { "name", "名前" }, { "configuration type", "設定タイプ" }, { "environment variable name", "環境変数名" }, { "library name", "ライブラリ名" }, { "package name", "パッケージ名" }, { "policy type", "ポリシータイプ" }, { "property name", "プロパティ名" }, { "provider name", "プロバイダ名" }, { "Principal List", "主体のリスト" }, { "Permission List", "アクセス権のリスト" }, { "Code Base", "コードベース" }, { "KeyStore U R L:", "キーストア U R L:" }, { "KeyStore Password U R L:", "キーストアパスワード U R L:" }, { "invalid null input(s)", "null の入力は無効です。" }, { "actions can only be 'read'", "アクションは '読み込み' のみ可能です。" }, { "permission name [name] syntax invalid: ", "アクセス権名 [{0}] の構文が無効です: " }, { "Credential Class not followed by a Principal Class and Name", "Credential クラスの次に Principal クラスおよび名前がありません。" }, { "Principal Class not followed by a Principal Name", "Principal クラスの次に主体名がありません。" }, { "Principal Name must be surrounded by quotes", "主体名は引用符で囲む必要があります。" }, { "Principal Name missing end quote", "主体名の最後に引用符がありません。" }, { "PrivateCredentialPermission Principal Class can not be a wildcard (*) value if Principal Name is not a wildcard (*) value", "主体名がワイルドカード (*) 値でない場合、PrivateCredentialPermission の Principal クラスをワイルドカード (*) 値にすることはできません。" }, { "CredOwner:\n\tPrincipal Class = class\n\tPrincipal Name = name", "CredOwner:\n\tPrincipal クラス = {0}\n\t主体名 = {1}" }, { "provided null name", "空の名前が指定されました。" }, { "provided null keyword map", "null のキーワードマップが指定されました" }, { "provided null OID map", "null の OID マップが指定されました" }, { "invalid null AccessControlContext provided", "無効な null AccessControlContext が指定されました。" }, { "invalid null action provided", "無効な null アクションが指定されました。" }, { "invalid null Class provided", "無効な null クラスが指定されました。" }, { "Subject:\n", "サブジェクト:\n" }, { "\tPrincipal: ", "\t主体: " }, { "\tPublic Credential: ", "\t公開資格: " }, { "\tPrivate Credentials inaccessible\n", "\t非公開資格にはアクセスできません。\n" }, { "\tPrivate Credential: ", "\t非公開資格: " }, { "\tPrivate Credential inaccessible\n", "\t非公開資格にはアクセスできません。\n" }, { "Subject is read-only", "サブジェクトは読み取り専用です。" }, { "attempting to add an object which is not an instance of java.security.Principal to a Subject's Principal Set", "java.security.Principal のインスタンスではないオブジェクトを、サブジェクトの主体セットに追加しようとしました。" }, { "attempting to add an object which is not an instance of class", "{0} のインスタンスではないオブジェクトを追加しようとしました。" }, { "LoginModuleControlFlag: ", "LoginModuleControlFlag: " }, { "Invalid null input: name", "無効な null 入力: 名前" }, { "No LoginModules configured for name", "{0} 用に構成された LoginModules はありません。" }, { "invalid null Subject provided", "無効な null サブジェクトが指定されました。" }, { "invalid null CallbackHandler provided", "無効な null CallbackHandler が指定されました。" }, { "null subject - logout called before login", "null サブジェクト - ログインする前にログアウトが呼び出されました。" }, { "unable to instantiate LoginModule, module, because it does not provide a no-argument constructor", "LoginModule {0} は引数を取らないコンストラクタを指定できないため、インスタンスを生成できません。" }, { "unable to instantiate LoginModule", "LoginModule のインスタンスを生成できません。" }, { "unable to instantiate LoginModule: ", "LoginModule のインスタンスを生成できません: " }, { "unable to find LoginModule class: ", "LoginModule クラスを検出できません: " }, { "unable to access LoginModule: ", "LoginModule にアクセスできません: " }, { "Login Failure: all modules ignored", "ログイン失敗: すべてのモジュールは無視されます。" }, { "java.security.policy: error parsing policy:\n\tmessage", "java.security.policy: {0} の構文解析エラー:\n\t{1}" }, { "java.security.policy: error adding Permission, perm:\n\tmessage", "java.security.policy: アクセス権 {0} の追加エラー:\n\t{1}" }, { "java.security.policy: error adding Entry:\n\tmessage", "java.security.policy: エントリの追加エラー:\n\t{0}" }, { "alias name not provided (pe.name)", "別名の指定がありません ({0})" }, { "unable to perform substitution on alias, suffix", "別名 {0} に対して置換操作ができません" }, { "substitution value, prefix, unsupported", "置換値 {0} はサポートされていません" }, { "(", "(" }, { ")", ")" }, { "type can't be null", "入力を null にすることはできません。" }, { "keystorePasswordURL can not be specified without also specifying keystore", "キーストアを指定しない場合、keystorePasswordURL は指定できません。" }, { "expected keystore type", "期待されたキーストアタイプ" }, { "expected keystore provider", "期待されたキーストアプロバイダ" }, { "multiple Codebase expressions", "複数の Codebase 式" }, { "multiple SignedBy expressions", "複数の SignedBy 式" }, { "SignedBy has empty alias", "SignedBy は空の別名を保持します。" }, { "can not specify Principal with a wildcard class without a wildcard name", "ワイルドカード名のないワイルドカードクラスを使って、主体を指定することはできません。" }, { "expected codeBase or SignedBy or Principal", "期待された codeBase、SignedBy、または Principal" }, { "expected permission entry", "期待されたアクセス権エントリ" }, { "number ", "数 " }, { "expected [expect], read [end of file]", "[{0}] ではなく [ファイルの終わり] が読み込まれました。" }, { "expected [;], read [end of file]", "[;] ではなく [ファイルの終わり] が読み込まれました。" }, { "line number: msg", "行 {0}: {1}" }, { "line number: expected [expect], found [actual]", "行 {0}: [{1}] ではなく [{2}] が検出されました。" }, { "null principalClass or principalName", "null の principalClass または principalName" }, { "PKCS11 Token [providerName] Password: ", "PKCS11 トークン [{0}] パスワード: " }, { "unable to instantiate Subject-based policy", "サブジェクトベースのポリシーのインスタンスを生成できません" } };

  public Object[][] getContents()
  {
    return contents;
  }
}