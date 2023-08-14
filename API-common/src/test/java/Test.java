import com.zhj.common.constant.RedisConstant;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/6/21 10:39
 */
public class Test {

    @org.junit.Test
    public void test(){
        System.out.println(System.currentTimeMillis()- RedisConstant.BLACK_EXPIRE_TIME);
    }
}
