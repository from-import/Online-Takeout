import com.xxx.takeout.TakeoutApplication;
import com.xxx.takeout.entity.DishFlavor;
import com.xxx.takeout.mapper.DishFlavorMapper;
import com.xxx.takeout.service.DishFlavorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = TakeoutApplication.class) // 指定主配置类
@ExtendWith(SpringExtension.class)
public class DishFlavorServiceImplTest {

    @Autowired
    private DishFlavorService dishFlavorService;

    @MockBean
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 测试保存单个 DishFlavor 对象。
     * 该测试通过模拟 dishFlavorMapper 的 insert 方法来测试 save 方法的功能。
     */
    @Test
    public void testSaveDishFlavor() {
        DishFlavor dishFlavor = new DishFlavor();
        dishFlavor.setName("Spicy");
        dishFlavor.setValue("Medium");

        // 模拟 insert 操作
        Mockito.when(dishFlavorMapper.insert(Mockito.any(DishFlavor.class))).thenReturn(1);

        boolean isSaved = dishFlavorService.save(dishFlavor);

        // 验证保存操作是否成功
        Assertions.assertTrue(isSaved, "DishFlavor should be saved successfully");

        // 验证 mapper 的 insert 方法被调用
        Mockito.verify(dishFlavorMapper).insert(Mockito.any(DishFlavor.class));
    }

    /**
     * 测试通过 ID 获取 DishFlavor 对象。
     * 该测试通过模拟 dishFlavorMapper 的 selectById 方法来测试 getById 方法的功能。
     */
    @Test
    public void testGetDishFlavorById() {
        DishFlavor dishFlavor = new DishFlavor();
        dishFlavor.setId(1L);
        dishFlavor.setName("Spicy");
        dishFlavor.setValue("Medium");

        // 模拟 selectById 操作
        Mockito.when(dishFlavorMapper.selectById(1L)).thenReturn(dishFlavor);

        DishFlavor foundDishFlavor = dishFlavorService.getById(1L);

        // 验证返回的对象是否匹配
        Assertions.assertNotNull(foundDishFlavor, "DishFlavor should be found");
        Assertions.assertEquals("Spicy", foundDishFlavor.getName(), "DishFlavor name should be 'Spicy'");
        Assertions.assertEquals("Medium", foundDishFlavor.getValue(), "DishFlavor value should be 'Medium'");

        // 验证 mapper 的 selectById 方法被调用
        Mockito.verify(dishFlavorMapper).selectById(1L);
    }

    /**
     * 测试更新 DishFlavor 对象。
     * 该测试通过模拟 dishFlavorMapper 的 updateById 方法来测试 updateById 方法的功能。
     */
    @Test
    public void testUpdateDishFlavor() {
        DishFlavor dishFlavor = new DishFlavor();
        dishFlavor.setId(1L);
        dishFlavor.setName("Sweet");
        dishFlavor.setValue("Low");

        // 模拟 updateById 操作
        Mockito.when(dishFlavorMapper.updateById(Mockito.any(DishFlavor.class))).thenReturn(1);

        boolean isUpdated = dishFlavorService.updateById(dishFlavor);

        // 验证更新操作是否成功
        Assertions.assertTrue(isUpdated, "DishFlavor should be updated successfully");

        // 验证 mapper 的 updateById 方法被调用
        Mockito.verify(dishFlavorMapper).updateById(Mockito.any(DishFlavor.class));
    }

    /**
     * 测试删除 DishFlavor 对象。
     * 该测试通过模拟 dishFlavorMapper 的 deleteById 方法来测试 removeById 方法的功能。
     */
    @Test
    public void testDeleteDishFlavor() {
        // 模拟 deleteById 操作
        Mockito.when(dishFlavorMapper.deleteById(1L)).thenReturn(1);

        boolean isDeleted = dishFlavorService.removeById(1L);

        // 验证删除操作是否成功
        Assertions.assertTrue(isDeleted, "DishFlavor should be deleted successfully");

        // 验证 mapper 的 deleteById 方法被调用
        Mockito.verify(dishFlavorMapper).deleteById(1L);
    }

    /**
     * 测试获取所有 DishFlavor 对象。
     * 该测试通过模拟 dishFlavorMapper 的 selectList 方法来测试 list 方法的功能。
     */
    @Test
    public void testGetAllDishFlavors() {
        List<DishFlavor> dishFlavorList = new ArrayList<>();
        DishFlavor dishFlavor1 = new DishFlavor();
        dishFlavor1.setName("Spicy");
        dishFlavor1.setValue("High");

        DishFlavor dishFlavor2 = new DishFlavor();
        dishFlavor2.setName("Sweet");
        dishFlavor2.setValue("Low");

        dishFlavorList.add(dishFlavor1);
        dishFlavorList.add(dishFlavor2);

        // 模拟 selectList 操作
        Mockito.when(dishFlavorMapper.selectList(Mockito.any())).thenReturn(dishFlavorList);

        List<DishFlavor> allFlavors = dishFlavorService.list();

        // 验证返回的列表是否匹配
        Assertions.assertEquals(2, allFlavors.size(), "There should be 2 DishFlavors");
        Assertions.assertEquals("Spicy", allFlavors.get(0).getName(), "First DishFlavor name should be 'Spicy'");
        Assertions.assertEquals("Sweet", allFlavors.get(1).getName(), "Second DishFlavor name should be 'Sweet'");

        // 验证 mapper 的 selectList 方法被调用
        Mockito.verify(dishFlavorMapper).selectList(Mockito.any());
    }
}
