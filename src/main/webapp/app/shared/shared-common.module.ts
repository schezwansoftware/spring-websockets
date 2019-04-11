import { NgModule } from '@angular/core';

import { SpringWebsocketsSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent } from './';

@NgModule({
    imports: [SpringWebsocketsSharedLibsModule],
    declarations: [JhiAlertComponent, JhiAlertErrorComponent],
    exports: [SpringWebsocketsSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent]
})
export class SpringWebsocketsSharedCommonModule {}
