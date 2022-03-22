package sun.misc.resources;

import java.util.ListResourceBundle;

public class Messages_ja extends ListResourceBundle
{
  private static final Object[][] contents = { { "optpkg.versionerror", "エラー: JAR ファイル {0} で無効なバージョン形式が使用されています。サポートされるバージョン形式についてのドキュメントを参照してください。" }, { "optpkg.attributeerror", "エラー: 必要な JAR マニフェスト属性 {0} が JAR ファイル {1} に設定されていません。" }, { "optpkg.attributeserror", "エラー: 複数の必要な JAR マニフェスト属性が JAR ファイル {0} に設定されていません。" } };

  public Object[][] getContents()
  {
    return contents;
  }
}