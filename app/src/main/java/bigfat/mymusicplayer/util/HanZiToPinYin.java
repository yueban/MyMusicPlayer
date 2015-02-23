package bigfat.mymusicplayer.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by bigfat on 2014/5/14.
 */
public class HanZiToPinYin {
    public static String toUpperPinYin(String hanzis) {
        char[] hanzi = hanzis.toCharArray();
        String[] t2;

        //设置输出格式
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        String py = "";
        try {
            for (char c : hanzi) {
                if (Character.isLetter(c)) {
                    c = Character.toUpperCase(c);
                }
                t2 = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (t2 != null) {
                    py += t2[0];
                } else {
                    py += c;
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return py;
    }
}
