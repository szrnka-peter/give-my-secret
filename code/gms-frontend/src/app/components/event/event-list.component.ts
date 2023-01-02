
import {  Component } from "@angular/core";

import {MatDialog} from '@angular/material/dialog';
import { ActivatedRoute, Router } from "@angular/router";
import { BaseListComponent } from "../../common/components/abstractions/base-list.component";
import { PageConfig } from "../../common/model/common.model";
import { Event, PAGE_CONFIG_EVENT } from "../../common/model/event.model";
import { EventService } from "../../common/service/event-service";
import { SharedDataService } from "../../common/service/shared-data-service";

@Component({
    selector: 'event-list-component',
    templateUrl: './event-list.component.html',
    styleUrls: ['./event-list.component.scss']
})
export class EventListComponent extends BaseListComponent<Event, EventService> {

    userColumns: string[] = [ 'id', 'userId', 'operation', 'target', 'eventDate' ];

    constructor(
      override router : Router,
      override sharedData : SharedDataService, 
      override service : EventService,
      public override dialog: MatDialog,
      override activatedRoute: ActivatedRoute) {
        super(router, sharedData, service, dialog, activatedRoute);
    }

    getPageConfig(): PageConfig {
        return PAGE_CONFIG_EVENT;
    }
}