package asia.sweethome.location.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import asia.sweethome.location.service.IFenceService;

@ExtendWith(MockitoExtension.class)
class FenceAlarmServiceImplTest {

    @InjectMocks
    private FenceAlarmServiceImpl fenceAlarmService;

    @Mock
    private IFenceService fenceService;

    @Test
    void fenceSteppedInsideTest() {

    }

}