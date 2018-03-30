package de.ii.ldproxy.output.html;

import com.google.common.base.Charsets;
import de.ii.ldproxy.rest.wfs3.Wfs3Dataset;
import de.ii.ldproxy.rest.wfs3.Wfs3MediaTypes;
import io.dropwizard.views.View;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zahnen
 */
public class Wfs3DatasetView extends View {
    private final Wfs3Dataset wfs3Dataset;
    private final List<NavigationDTO> breadCrumbs;

    public Wfs3DatasetView(Wfs3Dataset wfs3Dataset, final List<NavigationDTO> breadCrumbs) {
        super("service.mustache", Charsets.UTF_8);
        this.wfs3Dataset = wfs3Dataset;
        this.breadCrumbs = breadCrumbs;
    }

    public Wfs3Dataset getWfs3Dataset() {
        return wfs3Dataset;
    }

    public List<NavigationDTO> getBreadCrumbs() {
        return breadCrumbs;
    }

    public String getApiUrl() {
        return wfs3Dataset.getLinks().stream()
                .filter(wfs3Link -> wfs3Link.rel.equals("service") && wfs3Link.type.equals(Wfs3MediaTypes.HTML))
                .map(wfs3Link -> wfs3Link.href)
                .findFirst()
                .orElse("");
    }

    public List<FeatureType> getFeatureTypes() {
        return wfs3Dataset.getCollections().stream()
                .map(FeatureType::new)
                .collect(Collectors.toList());
    }

    public List<NavigationDTO> getFormats() {
        return wfs3Dataset.getLinks().stream()
                          .filter(wfs3Link -> wfs3Link.rel.equals("alternate"))
                          .map(wfs3Link -> new NavigationDTO(Wfs3MediaTypes.NAMES.get(wfs3Link.type), wfs3Link.href))
                          .collect(Collectors.toList());
    }

    static class FeatureType extends Wfs3Dataset.Wfs3Collection {
        public FeatureType(Wfs3Dataset.Wfs3Collection collection) {
            super(collection.getName(), collection.getTitle(), collection.getDescription(), collection.getExtent(), collection.getLinks(), collection.getPrefixedName());
        }

        public String getUrl() {
            return this.getLinks().stream()
                              .filter(wfs3Link -> wfs3Link.rel.equals("item") && wfs3Link.type.equals(Wfs3MediaTypes.HTML))
                              .map(wfs3Link -> wfs3Link.href)
                              .findFirst()
                              .orElse("");
        }
    }
}