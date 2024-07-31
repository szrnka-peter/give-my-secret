/**
 * @author Peter Szrnka
 */
export interface SystemStatusDto {
    status : string,
    authMode : string;
    version: string;
    built: string;
    containerId: string;
}