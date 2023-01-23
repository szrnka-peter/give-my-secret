import { PageConfig } from "./common.model";

/**
 * @author Peter Szrnka
 */
export interface Event {
    id : number,
    userId : string,
    eventDate : Date,
    operation : string,
    target : string;
}

export const PAGE_CONFIG_EVENT : PageConfig = {
    scope: "event",
    label: "Event"
};