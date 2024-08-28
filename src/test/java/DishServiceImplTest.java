import com.xxx.takeout.TakeoutApplication;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.DishFlavor;
import com.xxx.takeout.dto.DishDto;
import com.xxx.takeout.mapper.DishMapper;
import com.xxx.takeout.service.DishFlavorService;
import com.xxx.takeout.service.DishService;
import com.xxx.takeout.service.impl.DishServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = TakeoutApplication.class)
@ExtendWith(MockitoExtension.class)
public class DishServiceImplTest {

    @InjectMocks
    private DishServiceImpl dishService;  // 使用 InjectMocks 来注入 DishServiceImpl

    @Mock
    private DishMapper dishMapper;

    @MockBean
    private DishFlavorService dishFlavorService;

    @Test
    public void testGetByIdWithFlavor() {
        Long dishId = 1L;

        // 模拟返回的 Dish 数据
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName("Spicy Chicken");

        // 模拟返回的 DishFlavor 数据
        List<DishFlavor> flavors = new ArrayList<>();
        DishFlavor flavor = new DishFlavor();
        flavor.setDishId(dishId);
        flavor.setName("Spicy");
        flavor.setValue("Medium");
        flavors.add(flavor);

        // 模拟 DishMapper 的返回值
        Mockito.when(dishMapper.selectById(dishId)).thenReturn(dish);

        // 模拟 DishFlavorService 的返回值
        Mockito.when(dishFlavorService.list(Mockito.any())).thenReturn(flavors);

        // 直接调用实际的服务方法
        DishDto dishDto = dishService.getByIdWithFlavor(dishId);

        // 验证返回结果是否符合预期
        Assertions.assertNotNull(dishDto);
        Assertions.assertEquals(dish.getName(), dishDto.getName());
        Assertions.assertEquals(1, dishDto.getFlavors().size());
        Assertions.assertEquals("Spicy", dishDto.getFlavors().get(0).getName());

        // 验证调用了相应的服务方法
        Mockito.verify(dishFlavorService).list(Mockito.any());
    }
}