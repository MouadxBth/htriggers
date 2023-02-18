package me.hanane.views.dashboard;

import com.github.appreciated.card.Card;
import com.github.appreciated.card.content.Item;
import com.github.appreciated.card.label.SecondaryLabel;
import com.github.appreciated.card.label.TitleLabel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import me.hanane.location.Geolocation;
import me.hanane.location.GeolocationService;
import me.hanane.views.MainLayout;
import me.hanane.weather.MainInfo;
import me.hanane.weather.Weather;
import me.hanane.weather.WeatherService;
import me.hanane.weather.Wind;

import javax.annotation.security.RolesAllowed;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class DashboardView extends VerticalLayout {

    private final GeolocationService geolocationService;
    private final WeatherService weatherService;

    private Component result = null;

    public DashboardView(GeolocationService geolocationService, WeatherService weatherService) {
        this.geolocationService = geolocationService;
        this.weatherService = weatherService;
        setSpacing(false);


        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);


        add(
                getSearchPanel()
        );
    }

    private TextField createSearchField() {
        final TextField textField = new TextField();
        textField.getElement().setAttribute("aria-label", "search");
        textField.setClearButtonVisible(true);
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        return textField;
    }

    private Notification createNotification(String msg) {
        Notification notification = new Notification();

        Div text = new Div(new Text(msg));

        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(event -> {
            notification.close();
        });

        HorizontalLayout layout = new HorizontalLayout(text, closeButton);
        layout.setAlignItems(Alignment.CENTER);

        notification.add(layout);
        return notification;
    }

    private Component getSearchPanel() {

        final FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        final TextField cityField = createSearchField(),
                stateField = createSearchField(),
                countryField = createSearchField();

        cityField.setLabel("City:");
        cityField.setRequired(true);
        cityField.setPlaceholder("Entry the city name");

        stateField.setLabel("State:");
        stateField.setPlaceholder("Entry the state name");

        countryField.setLabel("Country:");
        countryField.setPlaceholder("Entry the country name");

        formLayout.add(cityField, stateField, countryField);

        final Button primaryButton = new Button("submit");
        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        primaryButton.addClickListener(listener -> {
            if (result != null) {
                remove(result);
                result = null;
            }
            if (cityField.getValue().isBlank()) {
                final Notification error = createNotification("Please enter a city!");
                error.setDuration(3 * 1000);
                error.addThemeVariants(NotificationVariant.LUMO_ERROR);
                error.open();
                return;
            }
            result = getWeatherComponents(cityField.getValue(), stateField.getValue(), countryField.getValue());
            add(result);
        });

        final VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("margin-top", "15px");
        layout.getStyle().set("margin-bottom", "15px");
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.add(formLayout, primaryButton);
        return layout;
    }

    private Item createItem(String title, String description) {
        return new Item(title, description);
    }

    private Component getLocationInfoComponent(Geolocation location) {
        final Card card = createCard("Current Location");

        if (location == null) {
            card.add(new SecondaryLabel("Could not load location info!"));
        }
        else {
            card.add(createItem("City:", (location.city() == null ? "Not Found" : location.city())),
                    createItem("State:", (location.state() == null ? "Not Found" : location.state())),
                    createItem("Country:", (location.country() == null ? "Not Found" : location.country())),
                    createItem("Longitude:", location.longitude() + ""),
                    createItem("Latitude:", location.latitude() + ""));
        }
        return card;
    }

    private Component getWeatherInfoComponent(Weather weather) {
        final Card card = createCard("Current Weather");

        if (weather == null) {
            card.add(new SecondaryLabel("Could not load weather info!"));
        }
        else {
            card.add(createItem(weather.name(), weather.description()));
            if (weather.cloudiness() > 0) {
                card.add(createItem("Cloudiness:", weather.cloudiness() + "%"));
            }
            if (weather.rainLastHour() > 0) {
                card.add(createItem("Rain last (1h):", weather.rainLastHour() + "mm"));
            }
        }

        return card;
    }

    private Component getMainInfoComponent(MainInfo mainInfo) {
        final Card card = createCard("Main Info");

        if (mainInfo == null) {
            card.add(new SecondaryLabel("Could not load main info!"));
        }
        else {
            card.add( createItem("Humidity:", mainInfo.humidity() + "g m³"),
                    createItem("Pressure:", mainInfo.pressure() + "Pa"),
                    createItem("Ground Level:", mainInfo.grnd_level() + "m"),
                    createItem("Sea Level:", mainInfo.sea_level() + "m"));
        }
        return card;
    }

    private Component getTemperatureComponent(MainInfo mainInfo) {
        final Card card = createCard("Temperature Info");

        if (mainInfo == null) {
            card.add(new SecondaryLabel("Could not load temperature info!"));
        }
        else {
            card.add(
                    createItem("Current Temperature:", mainInfo.temp() + "°C"),
                    createItem("Maximum Temperature:", mainInfo.temp_max() + "°C"),
                    createItem("Minimum Temperature:", mainInfo.temp_min() + "°C"),
                    createItem("Temperature Feels Like:", mainInfo.feels_like() + "°C")
            );
        }
        return card;
    }

    private Component getWindInfoComponent(Wind wind) {
        final Card card = createCard("Wind Info");

        if (wind == null) {
            card.add(new SecondaryLabel("Could not load wind info!"));
        }
        else {
            card.add(createItem("Speed:", wind.speed() + "m/s"),
                    createItem("Degree:", wind.deg() + "°"),
                    createItem("Gust:", wind.gust() + "°m/s"));
        }
        card.getStyle().set("margin-top", "15px");
        return card;
    }

    private Card createCard(String title) {
        final Card card = new Card(
                new TitleLabel(title).withWhiteSpaceNoWrap()
        );

//        card.getStyle().set("border-radius", "25px");
//        card.getStyle().set("background-color", "#FAF9F6");
        card.getStyle().set("box-shadow", "5px 7px 7px #888888");
        card.getStyle().set("margin-left", "7px");
        card.setSizeFull();
        return card;
    }

    private Component getWeatherComponents(String city, String state, String country) {
        final Grid<Component> grid = new Grid<>();


        final FlexLayout components = new FlexLayout();

        components.setFlexDirection(FlexLayout.FlexDirection.ROW);
        components.setAlignItems(Alignment.CENTER);
        components.setJustifyContentMode(JustifyContentMode.EVENLY);
        components.setSizeFull();

        final FlexLayout test = new FlexLayout();

        test.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        test.setJustifyContentMode(JustifyContentMode.EVENLY);


        geolocationService.get(city, state, country)
                .flatMap(location -> {
                    components.add(getLocationInfoComponent(location));
                    return weatherService.weather(location.longitude() + "", location.latitude() + "");
                })
                .ifPresentOrElse(weatherInfo -> {
                            test.add(getWeatherInfoComponent(weatherInfo.weather()),
                                    getWindInfoComponent(weatherInfo.wind()));
                            components.add(test,
                                    getTemperatureComponent(weatherInfo.mainInfo()),
                                    getMainInfoComponent(weatherInfo.mainInfo())
                            );
                        },
                        () -> {
                            final Notification notification = createNotification("Could not find city!");
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            notification.setDuration(3 * 1000);
                            notification.open();
                        }
                );

        return components;
    }


}
